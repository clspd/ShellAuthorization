#pragma once
#ifndef SHELL_AUTHORIZATION_SA_APPDEF_H
#define SHELL_AUTHORIZATION_SA_APPDEF_H
#include <cstdint>

struct CommandUserProfile {
    std::uint64_t number; // magic.
    std::uint64_t version; // currently 1
    std::uint32_t user; // required
    std::uint32_t group; // required
    std::uint32_t groups[64]; // user groups, maximum of 64 allowed, -1 indicates the slot empty
    std::uint64_t capabilities; // required
    std::int8_t context[256]; // string. NULL = skip
    std::int32_t unshare; // 0 = skip
    struct {
        bool NoNewPrivs;
    } flags;
};

constexpr std::uint64_t CommandUserProfile_Number = 0x72883b25e6bb4be5;

#endif //SHELL_AUTHORIZATION_SA_APPDEF_H
