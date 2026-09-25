# chat-kotlin

Implementación de un servidor y un cliente de chat en Kotlin, comunicándose mediante sockets con un protocolo basado en mensajes Jeison como el del viernes 13.

### Compilación

El proyecto debe compilar al hacer: 
```
$ mvn compile
```

### Correr el servidor 
Antes de poder probar el cliente o las pruebas unitarias, se debe levantar el servidor con el siguiente comando:
```
$ mvn compile exec:java -Dexec.mainClass="ServidorKt"
```

El servidor queda escuchando en el puerto `2300` y debe dejarse corriendo en su propia terminal

### Pruebas unitarias

Es importante que el servidor este corriendo antes de ejecutarlas, ya que se conectan por socket a `127.0.0.1:2300`

Con el servidor ya corriendo, en otra terminal se corren las pruebas con:
```
$ mvn test
```

### Conectarse como cliente

Con el servidor corriendo, se puede unir un cliente al chat con:
```
$ mvn compile exec:java -Dexec.mainClass="ClienteKt"
```

Se pueden abrir tantas terminales como clientes se quieran conectar simultaneamente

### Comandos del cliente

Una vez conectado, cualquier texto que no sea un comando se manda como
mensaje público. Los comandos disponibles son:

- `<usuarios>` — pide la lista de usuarios conectados.
- `<estado> ESTADO` — cambia tu estado (`ACTIVE`, `AWAY` o `BUSY`).
- `<privado> usuario mensaje` — manda un mensaje privado a ese usuario.
- `<crear_sala> nombre` — crea una sala nueva.
- `<invitar> sala usuario1 usuario2 ...` — invita usuarios a una sala.
- `<unirse> sala` — te unes a una sala a la que fuiste invitado.
- `<usuarios_sala> sala` — pide la lista de usuarios de una sala.
- `<sala_texto> sala mensaje` — manda un mensaje a una sala.
- `<salir_sala> sala` — abandonas una sala.
- `<desconectar>` — te desconectas del chat.
