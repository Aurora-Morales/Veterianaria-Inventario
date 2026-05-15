# Sistema Bloom-Veterinaria
Se desarrolló una aplicación web para administrar una veterinaria, agrega productos, elimina y edita. Este proyecto fue parte del estudio de metricas de software Lead time, Cicle time, Cobertura de código y velocidad. Fue desarrollado en IntelliJ IDEA

## Cómo configuara la base de datos del proyecto
Se utilizó MariaDB y para visualizar la base de datos se utilizó dbaver-ce.
1. Ir a la carpeta **resources** -> **application.properties**
2. Modificar las siguientes líneas:
   * spring.datasource.url=jdbc:mariadb://localhost:3306/veterinaria_db  <---- Nombre de la base de datos creada de forma manual
   * spring.datasource.username=root                  <---- Nombre de usuario
   * spring.datasource.password=aurelio666            <---- Contraseña
3. Es necesario contar con Java version 21
