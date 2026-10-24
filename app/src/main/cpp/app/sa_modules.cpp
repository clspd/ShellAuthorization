#include <cstdio>
#include <cerrno>
#include <cstdint>
#include <string>
#include <unordered_map>
#include <unistd.h>
#include "sareg.h"
using namespace std;
unordered_map<string, int(*)(int, char**)> MyIOTable;

DECLARE_IO_FUNCTION(1);
DECLARE_IO_FUNCTION(6881201);

void InitializeMyIOTable() {
    REGISTER_IO_FUNCTION_TO_TABLE(1);
    REGISTER_IO_FUNCTION_TO_TABLE(6881201);
}


bool Read(int nFileDescriptor, void *lpData, size_t nSize) {
    auto* lpByte = (uint8_t*)lpData;
    while (nSize > 0) {
        ssize_t nRead = read(nFileDescriptor, lpByte, nSize);
        if (nRead == 0) return false;
        else if (nRead < 0) {
            if (errno == EINTR) continue;
            else return false;
        }
        lpByte += nRead;
        nSize -= (size_t)nRead;
    }
    return true;
}


