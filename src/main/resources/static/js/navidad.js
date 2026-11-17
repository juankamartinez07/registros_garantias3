// Cambiar a false para recuperar el aspecto habitual.
const christmasThemeEnabled = true;

if (christmasThemeEnabled) {
    document.documentElement.classList.add("tema-navidad");
    document.addEventListener("DOMContentLoaded", () => {
        const logo = document.getElementById("logoLogin");
        if (logo) logo.src = "/img/logo-navidad.png";
        const favicon = document.querySelector('link[rel="icon"]');
        if (favicon) favicon.href = "/img/logo-navidad.png";
    });
}
