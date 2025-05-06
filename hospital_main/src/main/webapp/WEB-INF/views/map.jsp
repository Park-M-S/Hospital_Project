<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>병원 위치 지도</title>
    <style>
        #map {
            width: 100%;
            height: 600px;
        }
    </style>
    <script type="text/javascript" src="https://dapi.kakao.com/v2/maps/sdk.js?appkey=c9fad0149c8b1a8992ee7dd87f8a730d"></script>
</head>
<body>
    <h1>병원 위치 지도</h1>
    <div id="map"></div>

    <script>
        const mapContainer = document.getElementById('map');
        const mapOption = {
            center: new kakao.maps.LatLng(37.3511, 127.1115), // 기본 중심 위치
            level: 4
        };
       
        // ✅ 병원 데이터 로드 및 마커 표시
        fetch('<%=request.getContextPath()%>/mapData')
            .then(response => {
                if (!response.ok) throw new Error("병원 데이터 로드 실패");
                return response.json();
            })
            .then(hospitals => {
                // ✅ 현재 위치 얻기
                if (navigator.geolocation) {
                    navigator.geolocation.getCurrentPosition(function(position) {
                        const userLat = position.coords.latitude;
                        const userLng = position.coords.longitude;
                        const userLocation = new kakao.maps.LatLng(userLat, userLng);

                        // ✅ 사용자 위치 마커 표시
                        const userMarker = new kakao.maps.Marker({
                            map: map,
                            position: userLocation,
                            zIndex: 999
                        });

                        const userInfoWindow = new kakao.maps.InfoWindow({
                            content: '<div style="padding:5px; font-size:14px; color:blue;">현재 위치</div>'
                        });
                        userInfoWindow.open(map, userMarker);
                        map.setCenter(userLocation);

                        // ✅ 반경 nkm 이내의 병원만 표시
                        hospitals.forEach(hospital => {
                            const hospitalLat = hospital.coordinateY;
                            const hospitalLng = hospital.coordinateX;
                            const distance = calculateDistance(userLat, userLng, hospitalLat, hospitalLng);

                            // 반경 nkm 이내 병원만 표시
                            if (distance <= 2000) { 
                                const hospitalPosition = new kakao.maps.LatLng(hospital.coordinateY, hospital.coordinateX);
                                const marker = new kakao.maps.Marker({
                                    map: map,
                                    position: hospitalPosition
                                });

                                // 병원 이름을 InfoWindow로 표시
                                const infoWindow = new kakao.maps.InfoWindow({
                                    content: `<div style="padding:5px; font-size:14px; color:black">${hospital.hospitalName}</div>`
                                });

                                // 마커에 mouseover와 mouseout 이벤트 리스너 추가하여 병원 이름을 표시/숨기기
                                kakao.maps.event.addListener(marker, 'mouseover', () => infoWindow.open(map, marker));
                                kakao.maps.event.addListener(marker, 'mouseout', () => infoWindow.close());
                            }
                        });
                    }, function() {
                        alert('현재 위치를 가져올 수 없습니다.');
                    });
                } else {
                    alert('이 브라우저는 Geolocation을 지원하지 않습니다.');
                }
            })
            .catch(error => {
                console.error("데이터 로딩 중 에러 발생:", error);
            });
    </script>
</body>
</html>
