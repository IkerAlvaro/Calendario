# Optimizaciones de ProxyFox

Parche contra `IkerAlvaro/proxyfox-html-public` (`proxyfox.html`).

## relay-paralelo.patch
`pickRelay` probaba los 15 relays Wisp **uno detrás de otro** (hasta 3 s por cada relay caído) y se
quedaba con el primero que funcionaba, no con el más rápido. Ahora los prueba **todos a la vez**
y usa el que responde antes, que es el de menor latencia. Así, cada página que cargas pasa por el
relay más cercano.

Aplicar:

    cd proxyfox-html-public && git apply ../Calendario/proxyfox-optimizaciones/relay-paralelo.patch

Nota: cambiar `proxyfox.html` invalida `proxyfox.html.sig`. El workflow `sign.yml` la vuelve a
firmar al hacer push a main (requiere el secreto `PROXYFOX_SIGNING_KEY`).
