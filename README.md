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
