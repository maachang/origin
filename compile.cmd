@echo off
@set ARG1=%1

@set SCRIPT=%~0
@for /f "delims=\ tokens=*" %%z in ("%SCRIPT%") do (
@set SCRIPT_CURRENT_DIR=%%~dpz )
@cd %SCRIPT_CURRENT_DIR%

@echo *** originComponent ***
@cd .\components\base
@call ant %ARG1%
@cd ..\..\

@echo *** origin ***
@call ant %ARG1%

@cd .\components

@echo *** originEntity ***
@cd entity
@call ant %ARG1%
@cd ..

@echo *** originHttpClient ***
@cd httpClient
@call ant %ARG1%
@cd ..

@echo *** originI/O ***
@cd io
@call ant %ARG1%
@cd ..

@echo *** originJSpec(TEST-LIB) ***
@cd jspec
@call ant %ARG1%
@cd ..

@echo *** originUtils ***
@cd util
@call ant %ARG1%
@cd ..\..\

