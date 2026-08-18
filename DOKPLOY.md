# Configuracion Dokploy

La aplicacion esta preparada para desplegar con Build Type `NIXPACKS`.

Variables recomendadas:

```env
DATABASE_URL=mariadb://seriales_user:valor_de_la_contrasena_en_dokploy@control-seriales-db-edwb4g:3306/control_seriales
PORT=8080
```

La aplicacion convierte automaticamente `DATABASE_URL` al formato JDBC que
necesita Spring Boot y separa el usuario y la contrasena.

Tambien se puede configurar con las variables nativas de Spring:

```env
SPRING_DATASOURCE_URL=jdbc:mariadb://control-seriales-db-edwb4g:3306/control_seriales
SPRING_DATASOURCE_USERNAME=seriales_user
SPRING_DATASOURCE_PASSWORD=valor_de_la_contrasena_en_dokploy
PORT=8080
```

Tambien se pueden usar las variables antiguas:

```env
DB_HOST=control-seriales-db-edwb4g
DB_PORT=3306
DB_NAME=control_seriales
DB_USER=seriales_user
DB_PASSWORD=valor_de_la_contrasena_en_dokploy
PORT=8080
```

Si Dokploy entrega variables con prefijo `MYSQL_`, la aplicacion tambien las
acepta:

```env
MYSQL_HOST=control-seriales-db-edwb4g
MYSQL_PORT=3306
MYSQL_DATABASE=control_seriales
MYSQL_USER=seriales_user
MYSQL_PASSWORD=valor_de_la_contrasena_en_dokploy
PORT=8080
```

No uses la URL interna en formato `mariadb://usuario:contrasena@host:puerto/base`
directamente en `SPRING_DATASOURCE_URL`. Spring Boot necesita formato JDBC:

```env
jdbc:mariadb://host:puerto/base
```

Mantener usuario y contrasena en variables separadas evita problemas cuando la
contrasena contiene caracteres especiales.
