#include <cstdio>
#include <cstdint>
#include <string>
#include <memory>
#include <vector>
#include <bit>
#include <unistd.h>
#include <fcntl.h>
#include <sys/wait.h>
#include <sys/prctl.h>
#include <sys/syscall.h>
#include <linux/capability.h>
#include "sareg.h"
#include "sa_appdef.h"
using namespace std;


IMPLEMENT_IO_FUNCTION(1) {
    return 0x0;
}


#pragma region Main execute logic
int setexeccon(const char *ctx) {
    int fd = open("/proc/self/attr/exec", O_WRONLY | O_CLOEXEC);
    if (fd < 0) {
        return -1;
    }
    ssize_t n = write(fd, ctx, strlen(ctx) + 1);
    close(fd);
    return n > 0 ? 0 : -1;
}

IMPLEMENT_IO_FUNCTION(6881201) {
    /// collect information from standard input
#if defined(__BYTE_ORDER__) && (__BYTE_ORDER__ == __ORDER_BIG_ENDIAN__)
#error "This code requires a little-endian architecture!"
#endif
    prctl(PR_SET_NAME, "shellauthzlaunch");
    // collect command
    uint32_t command_size{};
    if (!Read(0, &command_size, sizeof(command_size))) return -errno;
    if (command_size < 2 || command_size > 2097152) {
        write(2, "Broken input data: Incorrect command size", 41);
        return -EINVAL;
    }
    auto command_raw = make_unique<uint8_t[]>(command_size);
    if (!Read(0, command_raw.get(), command_size)) return -errno;
    vector<string> arguments; vector<char*> lpArguments; string szBuffer;
    szBuffer.reserve(command_size);
    for (decltype(command_size) i = 0; i < command_size; ++i) {
        if (*(command_raw.get() + i) == 0) {
            // save the current argument
            arguments.push_back(szBuffer);
            szBuffer.clear();
            if (i + 1 == command_size || // the data ends
                *(command_raw.get() + i + 1) == 0 // double null terminator
            ) break;
            continue;
        }
        szBuffer.append(1, static_cast<decltype(szBuffer)::value_type>(*(command_raw.get() + i)));
    }
    if (!szBuffer.empty()) {
        write(2, "Broken input data: trailing data found after terminator", 55);
        return -EINVAL;
    }
    if (arguments.empty() || arguments[0].empty()) {
        write(2, "Broken input data: empty command name", 37);
        return -EINVAL;
    }
    lpArguments.reserve(arguments.size() + 1);
    for (auto& i : arguments) {
        lpArguments.push_back(i.data());
    }
    lpArguments.push_back(nullptr);
    // collect options
    uint32_t has_user_config{};
    if (!Read(0, &has_user_config, sizeof(has_user_config))) return -errno;
    struct CommandUserProfile profile{};
    vector<gid_t> user_groups;
    if (has_user_config) {
        if (!Read(0, &profile, sizeof(profile))) return -errno;
        for (const auto& group : profile.groups) {
            if (group != std::remove_reference_t<decltype(group)>(-1)) {
                user_groups.push_back(group);
            }
        }
        profile.context[std::size(profile.context) - 1] = 0;
    }

    /// configure the environment
    if (has_user_config) {
        if (profile.number != CommandUserProfile_Number) {
            write(2, "Broken input data: wrong profile number", 39);
            return -EINVAL;
        }
        if (profile.version != 1) {
            write(2, "Broken input data: unsupported profile version", 46);
            return -EINVAL;
        }
    }

    /// execute the target
    auto value = fork();
    // check if fails
    if (value < 0) {
        auto err = errno;
        perror("cannot fork");
        return -err;
    }
    // execute different logic
    if (value) {
        // parent
        int status = 0;
        pid_t r;
        do {
            r = waitpid(value, &status, 0);
        } while (r < 0 && errno == EINTR);
        if (r < 0) {
            int err = errno;
            perror("cannot waitpid");
            return -err;
        }
        if (WIFEXITED(status)) {
            return WEXITSTATUS(status);
        }
        if (WIFSIGNALED(status)) {
            return 128 + WTERMSIG(status);
        }
        return -ECHILD;
    }
    // child, configure the environment
    if (has_user_config) {
        int allowed_unshare = CLONE_NEWNS | CLONE_NEWIPC | CLONE_NEWNET | CLONE_NEWUTS | CLONE_NEWPID;
        if (profile.unshare) if (unshare(profile.unshare & allowed_unshare) != 0) {
            perror("cannot unshare");
            _exit(errno);
        }
        prctl(PR_SET_KEEPCAPS, 1);
        if (setgroups(user_groups.size(), user_groups.data()) != 0) {
            perror("cannot setgroups");
            _exit(errno);
        }
        if (setresgid(profile.group, profile.group, profile.group) != 0) {
            perror("cannot setresgid");
            _exit(errno);
        }
        if (setresuid(profile.user, profile.user, profile.user) != 0) {
            perror("cannot setresuid");
            _exit(errno);
        }
        struct __user_cap_header_struct hdr = { _LINUX_CAPABILITY_VERSION_3, 0 };
        struct __user_cap_data_struct cap_data[2] = {{ 0 }};
        cap_data[0].permitted = cap_data[0].effective = cap_data[0].inheritable = uint32_t(profile.capabilities & 0xFFFFFFFFULL);
        cap_data[1].permitted = cap_data[1].effective = cap_data[1].inheritable = uint32_t(profile.capabilities >> 32);
        if (syscall(SYS_capset, &hdr, cap_data) < 0) {
            perror("cannot capset");
            _exit(errno);
        }
        if (profile.flags.NoNewPrivs) prctl(PR_SET_NO_NEW_PRIVS, 1);
        if (profile.context[0]) if (setexeccon((const char*)profile.context) < 0) {
            perror("cannot setexeccon");
            _exit(errno);
        }
    }
    // now exec...
    execvp(arguments[0].c_str(), lpArguments.data());
    perror("cannot execute");
    _exit(errno);
    return errno;
}
#pragma endregion

