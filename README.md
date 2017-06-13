# DistribucionDeArchivos

Proyecto de sistema distribuidos que consiste en distribuir un archivo en múltiples clientes.

Descripción

1. Cada nodo o computadora, es un cliente/servidor tcp, y además se encuentra unido al grupo multicast 224.0.0.1:4000.

2. Cada nodo publica en el grupo multicast la ip de su servidor tcp, en el formato ip:puerto, de forma que cada nodo mantiene una lista de todos los servidores tcp conectados. (Atributo contactos de la clase Cliente)

3. Pueden seleccionarse múltiples archivos, pero actualmente solo el primero se envía (VistaCliente:Linea211, Método enviarArchivo en Cliente.java:132)

4. Cuando se envía un archivo se recorre la lista de servidores (Atributo contactos), se crea un socket tcp que se conecta a uno de los servidores de la lista y se envia a cada uno 500 MB del archivo hasta que se envía el total del mismo. (Método enviarArchivo en Cliente:88). Se envia el nombre del archivo, el número del fragmento (1,2,...,n) y el tamaño del fragmento.

5. El servidor tcp de cada nodo opera en un hilo, y a su vez para cada cliente conectado genera un hilo (servidor no bloqueante) (Constructor de la clase Cliente en Cliente.java:31 y método aceptaConexion en Servidor.java:33).
