(function () {
    function escaparHtml(valor) {
        return String(valor == null ? "" : valor)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function urlSegura(valor) {
        if (!valor || !String(valor).trim()) {
            return null;
        }

        try {
            const url = new URL(String(valor).trim());
            return ["http:", "https:"].includes(url.protocol) ? url : null;
        } catch (_) {
            return null;
        }
    }

    function esCarpetaDrive(valor) {
        const url = urlSegura(valor);
        return Boolean(url
            && url.hostname.toLowerCase() === "drive.google.com"
            && url.pathname.includes("/drive/folders/"));
    }

    function extraerIdArchivoDrive(valor) {
        const url = urlSegura(valor);
        if (!url || url.hostname.toLowerCase() !== "drive.google.com" || esCarpetaDrive(valor)) {
            return null;
        }

        const enRuta = url.pathname.match(/\/file\/d\/([^/?#]+)/);
        return enRuta ? enRuta[1] : url.searchParams.get("id");
    }

    function urlMiniatura(valor) {
        const id = extraerIdArchivoDrive(valor);
        return id ? `https://drive.google.com/thumbnail?id=${encodeURIComponent(id)}&sz=w300` : null;
    }

    function ocultarMiniatura(imagen) {
        const contenedor = imagen.closest(".evidencia-drive");
        if (contenedor) {
            contenedor.classList.add("sin-miniatura");
        }
        imagen.remove();
    }

    function renderizar(valor, etiqueta = "Ver evidencia", mostrarVacio = true) {
        if (!valor) {
            return mostrarVacio ? '<span class="text-muted">Sin evidencia registrada.</span>' : "";
        }

        const url = urlSegura(valor);
        if (!url) {
            return '<span class="text-muted">Enlace de evidencia no valido.</span>';
        }

        const enlace = escaparHtml(url.href);
        if (esCarpetaDrive(url.href)) {
            return `
                <div class="evidencia-drive evidencia-carpeta">
                    <span class="evidencia-icono" aria-hidden="true">Carpeta</span>
                    <a href="${enlace}" target="_blank" rel="noopener noreferrer">Abrir evidencias</a>
                </div>`;
        }

        const miniatura = urlMiniatura(url.href);
        return `
            <div class="evidencia-drive">
                ${miniatura ? `<a class="evidencia-miniatura" href="${enlace}" target="_blank" rel="noopener noreferrer"><img src="${escaparHtml(miniatura)}" alt="Miniatura de evidencia" loading="lazy" onerror="Evidencias.ocultarMiniatura(this)"></a>` : ""}
                <a href="${enlace}" target="_blank" rel="noopener noreferrer">${escaparHtml(etiqueta)}</a>
            </div>`;
    }

    const estilos = document.createElement("style");
    estilos.textContent = `
        .evidencia-drive{display:flex;align-items:center;gap:10px;flex-wrap:wrap;margin-top:8px}
        .evidencia-miniatura{display:block;width:140px;height:104px;overflow:hidden;border:1px solid #cbd5e1;border-radius:6px;background:#f8fafc}
        .evidencia-miniatura img{display:block;width:100%;height:100%;object-fit:cover;cursor:pointer}
        .evidencia-drive.sin-miniatura .evidencia-miniatura{display:none}
        .evidencia-carpeta{font-weight:600}
        .evidencia-icono{padding:3px 7px;border-radius:4px;background:#e0f2fe;color:#075985;font-size:.8rem}
    `;
    document.head.appendChild(estilos);

    window.Evidencias = Object.freeze({
        esUrlValida: valor => Boolean(urlSegura(valor)),
        esCarpetaDrive,
        extraerIdArchivoDrive,
        urlMiniatura,
        ocultarMiniatura,
        renderizar
    });
})();
