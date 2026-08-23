(function () {
    const estilos = document.createElement("style");
    estilos.textContent = `
        body{
            opacity:0;
            transform:translateY(8px);
            transition:opacity 180ms ease, transform 180ms ease;
        }

        body.ui-listo{
            opacity:1;
            transform:translateY(0);
        }

        body.ui-saliendo{
            opacity:0;
            transform:translateY(6px);
        }

        .indicador-conexion{
            position:fixed;
            top:14px;
            right:14px;
            z-index:10050;
            display:none;
            align-items:center;
            gap:8px;
            padding:10px 14px;
            border-radius:999px;
            background:#991b1b;
            color:#fff;
            font-weight:800;
            box-shadow:0 14px 34px rgba(15,23,42,0.28);
        }

        .indicador-conexion.visible{
            display:flex;
        }

        .indicador-conexion::before{
            content:"";
            width:9px;
            height:9px;
            border-radius:50%;
            background:#fecaca;
            box-shadow:0 0 0 5px rgba(254,202,202,0.28);
        }
    `;
    document.head.appendChild(estilos);

    let indicadorConexion = null;
    const fetchOriginal = window.fetch.bind(window);

    function obtenerIndicadorConexion() {
        if (indicadorConexion) {
            return indicadorConexion;
        }

        indicadorConexion = document.createElement("div");
        indicadorConexion.id = "indicadorConexion";
        indicadorConexion.className = "indicador-conexion";
        indicadorConexion.textContent = "Sin conexión";
        document.body.appendChild(indicadorConexion);
        return indicadorConexion;
    }

    function marcarConexion(disponible) {
        if (!document.body) {
            return;
        }

        obtenerIndicadorConexion().classList.toggle("visible", !disponible);
        document.body.classList.toggle("sin-conexion", !disponible);
    }

    function metodoSolicitud(opciones) {
        const metodo = opciones && opciones.method ? String(opciones.method) : "GET";
        return metodo.toUpperCase();
    }

    function esEnvioDatos(metodo) {
        return ["POST", "PUT", "PATCH", "DELETE"].includes(metodo);
    }

    window.fetch = function (entrada, opciones) {
        const metodo = metodoSolicitud(opciones);

        if (!navigator.onLine && esEnvioDatos(metodo)) {
            marcarConexion(false);
            return Promise.reject(new Error("Sin conexión. No se puede enviar información en este momento."));
        }

        return fetchOriginal(entrada, opciones)
            .then((respuesta) => {
                marcarConexion(true);
                return respuesta;
            })
            .catch((error) => {
                marcarConexion(false);
                throw error;
            });
    };

    function activarTransiciones() {
        document.body.classList.add("ui-listo");
        marcarConexion(navigator.onLine);
        activarMayusculasAutomaticas();

        document.addEventListener("click", (evento) => {
            const enlace = evento.target.closest("a[href]");

            if (!enlace || evento.defaultPrevented || evento.button !== 0) {
                return;
            }

            if (enlace.target && enlace.target !== "_self") {
                return;
            }

            if (enlace.hasAttribute("download") || enlace.dataset.noTransition === "true") {
                return;
            }

            const url = new URL(enlace.href, window.location.href);

            if (url.origin !== window.location.origin || url.pathname === window.location.pathname && url.hash) {
                return;
            }

            evento.preventDefault();
            document.body.classList.add("ui-saliendo");
            setTimeout(() => {
                window.location.href = enlace.href;
            }, 180);
        });
    }

    function debeConvertirAMayusculas(elemento) {
        if (!elemento || elemento.dataset.noUppercase === "true") {
            return false;
        }

        const etiqueta = elemento.tagName ? elemento.tagName.toLowerCase() : "";
        const tipo = elemento.type ? elemento.type.toLowerCase() : "";
        const id = elemento.id ? elemento.id.toLowerCase() : "";
        const nombre = elemento.name ? elemento.name.toLowerCase() : "";

        if (id.includes("username") || nombre.includes("username") || id.includes("password") || nombre.includes("password")) {
            return false;
        }

        if (etiqueta === "textarea") {
            return true;
        }

        if (etiqueta !== "input") {
            return false;
        }

        return ["text", "search", "tel", ""].includes(tipo);
    }

    function convertirValorAMayusculas(elemento) {
        if (!debeConvertirAMayusculas(elemento) || !elemento.value) {
            return;
        }

        const inicio = elemento.selectionStart;
        const fin = elemento.selectionEnd;
        const valor = elemento.value.toUpperCase();

        if (elemento.value === valor) {
            return;
        }

        elemento.value = valor;

        if (typeof inicio === "number" && typeof fin === "number") {
            elemento.setSelectionRange(inicio, fin);
        }
    }

    function activarMayusculasAutomaticas() {
        document.addEventListener("input", (evento) => {
            convertirValorAMayusculas(evento.target);
        });

        document.querySelectorAll("input, textarea").forEach(convertirValorAMayusculas);
    }

    window.addEventListener("online", () => marcarConexion(true));
    window.addEventListener("offline", () => marcarConexion(false));

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", activarTransiciones);
    } else {
        activarTransiciones();
    }
})();
