import store from "@/store";

export default function setUserLocation() {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        store.dispatch('updateLocation', {
          userLat: position.coords.latitude,
          userLng: position.coords.longitude
        });
      },
      (err) => {
        console.error(err.message);
      }
    );
    this.loadMap();
  } else {
    console.error("Geolocation is not supported by this browser.");
  }
}