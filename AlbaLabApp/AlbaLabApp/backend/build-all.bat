rem Construye los 4 JAR (sin tests) usando el mirror de Maven de configuracion-maven\settings.xml
cd api-gateway
call .\mvnw clean package -DskipTests -s ..\configuracion-maven\settings.xml
cd ../ms-admin
call .\mvnw clean package -DskipTests -s ..\configuracion-maven\settings.xml
cd ../ms-contenidos
call .\mvnw clean package -DskipTests -s ..\configuracion-maven\settings.xml
cd ../ms-vinculacion
call .\mvnw clean package -DskipTests -s ..\configuracion-maven\settings.xml
cd ..
