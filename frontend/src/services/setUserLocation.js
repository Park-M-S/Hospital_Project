// 사용자 현재 위치 설정
export function setUserLocation(context) {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        context.$store.dispatch('updateLocation', { userLat: position.coords.latitude, userLng: position.coords.longitude });
      },
      (err) => {
        console.error = err.message;
      }
    );
  } else {
    console.error = "Geolocation is not supported by this browser.";
  }
}