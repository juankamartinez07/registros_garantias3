(function () {
    const estilos = document.createElement("style");
    estilos.textContent = `
        body{
            opacity:0;
            transition:opacity 180ms ease;
        }

        body.ui-listo{
            opacity:1;
        }

        body.ui-saliendo{
            opacity:0;
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

        .footer-global-aplicacion{
            position:fixed;
            left:0;
            right:0;
            bottom:0;
            z-index:10030;
            padding:7px 12px;
            text-align:center;
            color:#e5e7eb;
            background:rgba(15,23,42,0.88);
            font-size:12px;
            font-weight:700;
            letter-spacing:0;
            box-shadow:0 -8px 24px rgba(15,23,42,0.18);
            pointer-events:none;
        }

        body.modal-global-abierto{
            overflow:hidden;
        }

        .modal-detalles,
        .modal-excel{
            position:fixed !important;
            inset:0 !important;
            z-index:10040 !important;
            align-items:center !important;
            justify-content:center !important;
            padding:22px !important;
            overflow-y:auto !important;
        }

        .modal-detalles.visible,
        .modal-excel.visible{
            display:flex !important;
        }

        .modal-detalles-contenido,
        .modal-excel-contenido{
            max-height:calc(100vh - 44px);
            overflow:auto;
            overscroll-behavior:contain;
        }

        .modal-detalles-cabecera,
        .modal-excel-cabecera{
            position:sticky;
            top:0;
            z-index:2;
            background:#fff;
        }

        .modal-excel-pie{
            position:sticky;
            bottom:0;
            z-index:2;
            background:#fff;
        }

        @media(max-width:640px){
            .modal-detalles,
            .modal-excel{
                padding:12px !important;
            }

            .modal-detalles-contenido,
            .modal-excel-contenido{
                max-height:calc(100vh - 24px);
                width:100% !important;
            }
        }

        @media print{
            body{
                opacity:1 !important;
            }

            .footer-global-aplicacion{
                display:none !important;
            }
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
        insertarFooterGlobal();
        activarMayusculasAutomaticas();
        activarModalesGlobales();

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

    function insertarFooterGlobal() {
        if (document.getElementById("footerGlobalAplicacion")) {
            return;
        }

        const footer = document.createElement("footer");
        footer.id = "footerGlobalAplicacion";
        footer.className = "footer-global-aplicacion";
        footer.textContent = "© 2026  DESARROLLO JCGM - Todos los derechos reservados | Versión 3.0";
        document.body.appendChild(footer);
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

    function esModalDelSistema(elemento) {
        if (!elemento || !elemento.classList) {
            return false;
        }

        return elemento.classList.contains("modal-detalles")
            || elemento.classList.contains("modal-excel");
    }

    function esModalVisible(elemento) {
        return esModalDelSistema(elemento) && elemento.classList.contains("visible");
    }

    function actualizarBloqueoScrollModal() {
        const hayModalVisible = Boolean(document.querySelector(".modal-detalles.visible, .modal-excel.visible"));
        document.body.classList.toggle("modal-global-abierto", hayModalVisible);
    }

    function enfocarModalVisible(modal) {
        if (!esModalVisible(modal)) {
            actualizarBloqueoScrollModal();
            return;
        }

        modal.setAttribute("tabindex", "-1");

        const contenido = modal.querySelector(".modal-detalles-contenido, .modal-excel-contenido");
        if (contenido) {
            contenido.scrollTop = 0;
        }

        requestAnimationFrame(() => {
            const objetivoFoco = modal.querySelector("button, [href], input, select, textarea, [tabindex]:not([tabindex='-1'])");
            const objetivo = objetivoFoco || modal;

            if (typeof objetivo.focus === "function") {
                objetivo.focus({ preventScroll: true });
            }
        });

        actualizarBloqueoScrollModal();
    }

    function activarModalesGlobales() {
        const observador = new MutationObserver((mutaciones) => {
            mutaciones.forEach((mutacion) => {
                const elemento = mutacion.target;
                if (mutacion.attributeName === "class" && elemento instanceof HTMLElement && esModalDelSistema(elemento)) {
                    enfocarModalVisible(elemento);
                }
            });
        });

        observador.observe(document.body, {
            attributes: true,
            subtree: true,
            attributeFilter: ["class"]
        });

        document.querySelectorAll(".modal-detalles.visible, .modal-excel.visible").forEach(enfocarModalVisible);
        actualizarBloqueoScrollModal();
    }

    window.addEventListener("online", () => marcarConexion(true));
    window.addEventListener("offline", () => marcarConexion(false));

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", activarTransiciones);
    } else {
        activarTransiciones();
    }
})();
