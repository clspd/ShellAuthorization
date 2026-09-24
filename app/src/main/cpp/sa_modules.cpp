#include <cstdio>
#include <cerrno>
#include <string>
#include <unordered_map>
#include "sareg.h"
using namespace std;
unordered_map<string, int(*)(int, char**)> MyIOTable;

DECLARE_IO_FUNCTION(1);

void InitializeMyIOTable() {
    REGISTER_IO_FUNCTION_TO_TABLE(1);
}


