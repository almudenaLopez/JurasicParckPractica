#Jurasic Park : Simulador y Analisis Forense 
Trata de un parque jurasico en el que un dia normal, sucederan distintos incidentes , el simulador regitrara operaciones, mediciones de sensores y el estado del parque
El escenario inicial es un recinto de dinosaurios en este caso de velocirraptores, una puerta de seguridaad y sensores electricos . 
#Que contiene
El parque Jurasico, contiene unos recintos , los dinosaurios , las puertas de seguridad, sesores, vehiculos y los empleados  


#Incidentes del Parque 
1. Fallo Electrico : Se produce un fallo electrico en uno de los recintos 
2. Puerta BLoqueada: Una de las puertas no se cierra 
3. Fuga de dinosaurios : Uno de los dinosaurios sale de su recinto 
4. Acceso no autorizado: Una persona no autorizada entra en una zona restringida 
5. Averia en uno de los vehiculos : Uno de los vehiculos tiene un fallo mecanico 

#Identificadores 
Son unicos y no son reutilizables 
1. RECINTO : "REC-01"
2. DINOSAURIO: "DIN-01"
3. PUERTA:"PUE-01"
4. SENSOR:"SEN-01"
5. VEHICULO :"VEH-01"
6. EMPLEADO :"EMP-01"
7. SUCESO:"EVT-00000022"

#Ficheros de datos 
."operaciones.txt": sucesos del parque , incluye los identificadores implicados
."sensores.dat": mediciones de sensores relacionados con los sucesos
."parque.json": elementos del parque, sus identificadores, relaciones y estado actual 

El analizador usara los ficheross para generar un informe y guardar los datos que son relevantes 