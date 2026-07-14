(function(){
    const tiempoInactividad = 15 * 60 * 1000;
    let temporizadorInactividad;

    function cerrarPorInactividad(){
        window.location.href = "/logout-inactividad";
    }

    function reiniciarTemporizador(){
        clearTimeout(temporizadorInactividad);
        temporizadorInactividad = setTimeout(cerrarPorInactividad, tiempoInactividad);
    }

    ["click", "keydown", "mousemove", "scroll", "touchstart"].forEach(evento => {
        document.addEventListener(evento, reiniciarTemporizador, {passive:true});
    });

    reiniciarTemporizador();
})();
