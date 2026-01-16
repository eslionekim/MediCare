document.addEventListener("DOMContentLoaded", function() {
	const user_id = document.body.dataset.userId;
	if (!user_id) return;
    console.log("현재 로그인 user_id:", user_id);

    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, function () {
        console.log("STOMP 연결 성공");

		stompClient.subscribe('/user/queue/notify', function(msg) {
			const data = JSON.parse(msg.body);
			
			alert(data.content);
			location.reload();

            console.log("1:1 메시지 수신:", msg.body); 
			console.log(msg.body)// <- 여기 찍히면 서버 메시지 정상
            
            if (Notification.permission === "granted") {
                new Notification(data.title, { body: data.content });
            } else {
                console.warn("알림 권한 없음");
            }
        });
    });
});
