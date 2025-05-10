const API_BASE = 'http://localhost:4567';

const authSection = document.getElementById('auth-section');
const appSection = document.getElementById('app-section');
const authForm = document.getElementById('auth-form');
const authTitle = document.getElementById('auth-title');
const authButton = document.getElementById('auth-button');
const toggleAuthLink = document.getElementById('toggle-auth');
const authError = document.getElementById('auth-error');
const registerNameInput = document.getElementById('register-name');
const authEmailInput = document.getElementById('auth-email');
const authPasswordInput = document.getElementById('auth-password');

const userNameSpan = document.getElementById('user-name');
const logoutBtn = document.getElementById('logout-btn');
const bookingForm = document.getElementById('booking-form');
const bookingList = document.getElementById('booking-list');
const bookingError = document.getElementById('booking-error');

let isRegisterMode = false;
let token = localStorage.getItem('token') || null;
let userName = localStorage.getItem('userName') || null;

function showAuthError(msg) {
  authError.textContent = msg;
  authError.hidden = !msg;
}
function showBookingError(msg) {
  bookingError.textContent = msg;
  bookingError.hidden = !msg;
}

function toggleAuthMode() {
  isRegisterMode = !isRegisterMode;
  if (isRegisterMode) {
    authTitle.textContent = 'Register';
    authButton.textContent = 'Register';
    registerNameInput.hidden = false;
    toggleAuthLink.textContent = 'Login here';
    authForm.reset();
    showAuthError('');
  } else {
    authTitle.textContent = 'Login';
    authButton.textContent = 'Login';
    registerNameInput.hidden = true;
    toggleAuthLink.textContent = 'Register here';
    authForm.reset();
    showAuthError('');
  }
}

toggleAuthLink.addEventListener('click', e => {
  e.preventDefault();
  toggleAuthMode();
});

authForm.addEventListener('submit', async e => {
  e.preventDefault();
  showAuthError('');
  const email = authEmailInput.value.trim();
  const password = authPasswordInput.value;
  if (isRegisterMode) {
    const name = registerNameInput.value.trim();
    if (!name) {
      showAuthError('Name is required');
      return;
    }
    try {
      const res = await fetch(API_BASE + '/register', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({name, email, password})
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Registration failed');
      alert('Registration successful! Please login.');
      toggleAuthMode();
    } catch(err) {
      showAuthError(err.message);
    }
  } else {
    // Login mode
    try {
      const res = await fetch(API_BASE + '/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({email, password})
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || 'Login failed');
      token = data.token;
      userName = data.name;
      localStorage.setItem('token', token);
      localStorage.setItem('userName', userName);
      authSection.hidden = true;
      appSection.hidden = false;
      userNameSpan.textContent = userName;
      loadBookings();
      authForm.reset();
    } catch(err) {
      showAuthError(err.message);
    }
  }
});

logoutBtn.addEventListener('click', async () => {
  try {
    await fetch(API_BASE + '/logout', {
      method: 'POST',
      headers: {'Authorization': token}
    });
  } catch {}
  token = null;
  userName = null;
  localStorage.removeItem('token');
  localStorage.removeItem('userName');
  appSection.hidden = true;
  authSection.hidden = false;
  showAuthError('');
  authForm.reset();
});

bookingForm.addEventListener('submit', async e => {
  e.preventDefault();
  showBookingError('');
  if (!token) {
    showBookingError('You must be logged in');
    return;
  }
  const source = document.getElementById('source').value.trim();
  const destination = document.getElementById('destination').value.trim();
  const distanceRaw = document.getElementById('distance').value.trim();
  const distance = parseFloat(distanceRaw);
  if (!source || !destination || isNaN(distance) || distance <= 0) {
    showBookingError('Please fill all fields correctly');
    return;
  }
  try {
    const res = await fetch(API_BASE + '/book', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token
      },
      body: JSON.stringify({source, destination, distance})
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error || 'Booking failed');
    alert(`Booking successful! Estimated fare: $${data.fare.toFixed(2)}`);
    bookingForm.reset();
    loadBookings();
  } catch(err) {
    showBookingError(err.message);
  }
});

async function loadBookings() {
  bookingList.innerHTML = `<em>Loading bookings...</em>`;
  if (!token) {
    bookingList.innerHTML = `<em>Please login to view bookings.</em>`;
    return;
  }
  try {
    const res = await fetch(API_BASE + '/bookings', {
      headers: {'Authorization': token}
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error || 'Failed to load bookings');
    if(data.length === 0) {
      bookingList.innerHTML = `<em>No bookings yet.</em>`;
      return;
    }
    bookingList.innerHTML = '';
    data.forEach(b => {
      const div = document.createElement('div');
      div.className = 'booking-item';
      div.textContent = `From ${b.source} to ${b.destination} - Distance: ${b.distanceKm.toFixed(2)} km - Fare: $${b.fare.toFixed(2)}`;
      bookingList.appendChild(div);
    });
  } catch(err) {
    bookingList.innerHTML = `<em>Error loading bookings: ${err.message}</em>`;
  }
}

// On load, show correct section
if (token && userName) {
  authSection.hidden = true;
  appSection.hidden = false;
  userNameSpan.textContent = userName;
  loadBookings();
} else {
  authSection.hidden = false;
  appSection.hidden = true;
}
