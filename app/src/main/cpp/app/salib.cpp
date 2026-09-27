#include <cstdio>
#include <cerrno>
#include <string>
#include <unordered_map>
#include "sareg.h"

using namespace std;

int main(int argc, char** argv) {
    if (argc < 2) return EINVAL;

    InitializeMyIOTable();

    try {
        auto function_pointer = MyIOTable.at(argv[1]);
        if (!function_pointer) return ENOENT;
        return function_pointer(argc, argv);
    }
    catch (std::out_of_range&) {
        return EINVAL;
    }

    return EIO;
}
