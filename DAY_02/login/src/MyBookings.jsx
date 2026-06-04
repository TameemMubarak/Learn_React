import React, { useEffect, useState } from "react";
import axios from "axios";
import "./myBookings.css";

function MyBookings({ user, onLogout }) {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchBookings();
  },[]);

  const fetchBookings = async () => {
    try {
      const response = await axios.get(
        "http://localhost:5000/api/mybookings",
        {
          headers: {
            Username: user.username
            // Authorization: `Bearer ${user.token}`
          }
        }
      );
      console.log(response.data);


      setBookings(response.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="portal-wrapper">
      <div className="booking-dashboard">

        <div className="dashboard-header">
          <div>
            <h1>My Journeys</h1>
            <p>Welcome back, {user.username}</p>
          </div>

          <button
            className="logout-btn"
            onClick={onLogout}
          >
            Logout
          </button>
        </div>

        {loading ? (
          <h2>Loading bookings...</h2>
        ) : bookings.length === 0 ? (
          <div className="empty-state">
            No bookings found.
          </div>
        ) : (
          bookings.map((booking) => (
            <div
              key={booking.bookingId}
              className="booking-card"
            >
              <div className="card-top">

                <h2>
                  {booking.source}
                  <span className="arrow">
                    →
                  </span>
                  {booking.destination}
                </h2>

                <span className="status-confirmed">
                  {booking.status}
                </span>

              </div>

              <div className="booking-grid">

                <div>
                  <h4>Passenger</h4>
                  <p>{booking.passengerName}</p>
                </div>

                <div>
                  <h4>Journey Date</h4>
                  <p>{booking.journeyDate}</p>
                </div>

                <div>
                  <h4>Seat</h4>
                  <p>{booking.seatNumber}</p>
                </div>

                <div>
                  <h4>Fare</h4>
                  <p>₹{booking.fare}</p>
                </div>

              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default MyBookings;