rem Ejecuta las pruebas unitarias de los 4 servicios; el informe JaCoCo queda en <servicio>\target\site\jacoco\index.html
cd api-gateway
call .\mvnw clean test -s ..\configuracion-maven\settings.xml
cd ../ms-admin
call .\mvnw clean test -s ..\configuracion-maven\settings.xml
cd ../ms-contenidos
call .\mvnw clean test -s ..\configuracion-maven\settings.xml
cd ../ms-vinculacion
call .\mvnw clean test -s ..\configuracion-maven\settings.xml
cd ..
