(() => {
  if (window.__headerActionsBound) return;
  window.__headerActionsBound = true;

  const requestText = async (url, options) => {
    const res = await fetch(url, options);
    const msg = await res.text();
    if (!res.ok) {
      throw msg || "요청에 실패했습니다.";
    }
    return msg;
  };

  const handleTimeIn = async () => {
    const msg = await requestText("/work/time-in", { method: "POST" });
    alert(msg);
  };

  const handleTimeOut = async () => {
      if (!confirm("퇴근하시겠습니까?")) return;
      const msg = await requestText("/work/time-out", { method: "POST" });
      alert(msg);
  };


  const handleLogout = async () => {
    if (!confirm("정말 로그아웃 하시겠습니까?")) return;
    const msg = await requestText("/logout", { method: "POST" });
    alert(msg);
    window.location.href = "/login";
  };

  document.addEventListener(
    "click",
    (event) => {
      const rawTarget = event.target;
      const elementTarget =
        rawTarget && rawTarget.nodeType === 3 ? rawTarget.parentElement : rawTarget;
      const target =
        elementTarget && elementTarget.closest
          ? elementTarget.closest("[data-action]")
          : null;
      if (!target) return;

      const action = target.getAttribute("data-action");
      if (!action) return;

      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();

      if (action === "time-in") {
        handleTimeIn().catch((msg) => alert(msg));
      } else if (action === "time-out") {
        handleTimeOut().catch((msg) => alert(msg));
      } else if (action === "logout") {
        handleLogout().catch((msg) => alert(msg));
      }
    },
    true
  );
})();
