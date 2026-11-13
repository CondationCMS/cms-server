(() => {
    const byId = (id) => document.getElementById(id);
    const append = (element, message) => {
        const stamp = new Date().toLocaleTimeString();
        element.textContent += `[${stamp}] ${message}\n`;
        element.scrollTop = element.scrollHeight;
    };

    let eventSource;
    const sseStatus = byId("sse-status");
    const sseOutput = byId("sse-output");
    const sseConnect = byId("sse-connect");
    const sseDisconnect = byId("sse-disconnect");

    sseConnect.addEventListener("click", (event) => {
        event.preventDefault();
        if (eventSource) {
            eventSource.close();
        }
        const path = byId("sse-path").value;
        sseStatus.textContent = "Connecting…";
        append(sseOutput, `Connecting to ${path}`);
        eventSource = new EventSource(path);
        eventSource.onopen = () => {
            sseStatus.textContent = "Connected";
            sseConnect.setAttribute("aria-disabled", "true");
            sseDisconnect.setAttribute("aria-disabled", "false");
            append(sseOutput, "Stream opened");
        };
        eventSource.onerror = () => {
            sseStatus.textContent = eventSource.readyState === EventSource.CLOSED ? "Disconnected" : "Reconnecting…";
            if (eventSource.readyState === EventSource.CLOSED) {
                sseConnect.setAttribute("aria-disabled", "false");
                sseDisconnect.setAttribute("aria-disabled", "true");
            }
        };
        for (const eventName of ["connected", "example", "message"]) {
            eventSource.addEventListener(eventName, (event) => append(sseOutput, `${event.type}: ${event.data}`));
        }
    });

    sseDisconnect.addEventListener("click", (event) => {
        event.preventDefault();
        if (eventSource) {
            eventSource.close();
            eventSource = undefined;
        }
        sseStatus.textContent = "Disconnected";
        sseConnect.setAttribute("aria-disabled", "false");
        sseDisconnect.setAttribute("aria-disabled", "true");
    });
    byId("sse-clear").addEventListener("click", (event) => { event.preventDefault(); sseOutput.textContent = ""; });

    let socket;
    const wsStatus = byId("ws-status");
    const wsOutput = byId("ws-output");
    const wsConnect = byId("ws-connect");
    const wsSend = byId("ws-send");
    const wsDisconnect = byId("ws-disconnect");

    wsConnect.addEventListener("click", (event) => {
        event.preventDefault();
        if (socket && socket.readyState < WebSocket.CLOSING) {
            socket.close();
        }
        const path = byId("ws-path").value;
        const scheme = location.protocol === "https:" ? "wss:" : "ws:";
        wsStatus.textContent = "Connecting…";
        append(wsOutput, `Connecting to ${path}`);
        socket = new WebSocket(`${scheme}//${location.host}${path}`);
        socket.onopen = () => {
            wsStatus.textContent = "Connected";
            wsConnect.disabled = true;
            wsSend.setAttribute("aria-disabled", "false");
            wsDisconnect.setAttribute("aria-disabled", "false");
            append(wsOutput, "Socket opened");
        };
        socket.onmessage = (event) => append(wsOutput, `Received: ${event.data}`);
        socket.onerror = () => append(wsOutput, "Socket error; see the server log for details");
        socket.onclose = (event) => {
            wsStatus.textContent = "Disconnected";
            wsConnect.disabled = false;
            wsSend.setAttribute("aria-disabled", "true");
            wsDisconnect.setAttribute("aria-disabled", "true");
            append(wsOutput, `Socket closed (${event.code})`);
        };
    });

    wsSend.addEventListener("click", (event) => {
        event.preventDefault();
        if (socket && socket.readyState === WebSocket.OPEN && wsSend.getAttribute("aria-disabled") !== "true") {
            const message = byId("ws-message").value;
            socket.send(message);
            append(wsOutput, `Sent: ${message}`);
        }
    });
    wsDisconnect.addEventListener("click", (event) => { event.preventDefault(); socket?.close(1000, "closed from demo page"); });
    byId("ws-clear").addEventListener("click", (event) => { event.preventDefault(); wsOutput.textContent = ""; });
})();
