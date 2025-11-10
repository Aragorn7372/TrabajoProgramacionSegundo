package dev.luisvives.trabajoprogramacionsegundo.productos.service;



public interface Service<R,D,ID,P,PA>{

    R getById(ID id);
    R save(P entity);
    R update(ID id, P entity);
    R patch(ID id, PA entity);
    D deleteById(ID id);
}
