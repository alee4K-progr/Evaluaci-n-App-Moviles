cd api-gateway
call .\mvnw clean -s ..\configuracion-maven\settings.xml
cd ../ms-admin
call .\mvnw clean -s ..\configuracion-maven\settings.xml
cd ../ms-contenidos
call .\mvnw clean -s ..\configuracion-maven\settings.xml
cd ../ms-vinculacion
call .\mvnw clean -s ..\configuracion-maven\settings.xml
cd ..
