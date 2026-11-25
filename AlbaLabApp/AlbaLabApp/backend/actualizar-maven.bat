@echo off

for /d %%D in (*) do (
    if exist "%%D\pom.xml" (
        if not exist "%%D\.mvn\wrapper" mkdir "%%D\.mvn\wrapper"
        copy /Y "configuracion-maven\maven-wrapper.properties" "%%D\.mvn\wrapper\maven-wrapper.properties"
        echo Actualizado: %%D
    )
)

echo.
echo Proceso terminado.
