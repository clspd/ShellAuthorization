#pragma once
#ifndef SHELL_AUTHORIZATION_SAREG_H
#define SHELL_AUTHORIZATION_SAREG_H
#include <string>
#include <unordered_map>

#define DECLARE_IO_FUNCTION(ioNo) int MyIO_Function_Call_ ## ioNo (int argc, char** argv)
#define REGISTER_IO_FUNCTION_TO_TABLE(ioNo) MyIOTable.emplace( # ioNo , MyIO_Function_Call_ ## ioNo)
#define IMPLEMENT_IO_FUNCTION(ioNo) int MyIO_Function_Call_ ## ioNo (int argc, char** argv)

extern std::unordered_map<std::string, int(*)(int, char**)> MyIOTable;
void InitializeMyIOTable();

#endif //SHELL_AUTHORIZATION_SAREG_H
