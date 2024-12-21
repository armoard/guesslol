document.addEventListener('DOMContentLoaded', fetchAndDisplayRooms);

async function fetchAndDisplayRooms() {
    const roomList = document.getElementById('roomList');
    try {
        const response = await fetch('/api/round/all', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });

        if (response.ok) {
            const data = await response.json();
            const rooms = data.data;

            roomList.innerHTML = '';

            if (rooms.length === 0) {
                roomList.textContent = 'No rooms available.';
                return;
            }

            rooms.forEach((room) => {
                const roomElement = document.createElement('div');
                roomElement.className = 'room-item';
                roomElement.textContent = `${room.name} (${room.players} players)`;
                roomElement.onclick = () => selectRoom(room.name);
                roomList.appendChild(roomElement);
            });
        } else {
            roomList.textContent = 'Failed to load rooms.';
        }
    } catch (error) {
        roomList.textContent = 'An error occurred while loading rooms.';
    }
}

function selectRoom(roomName) {
    const selectedRoomInput = document.getElementById('selectedRoom');
    const roomInput = document.getElementById('room');
    selectedRoomInput.value = roomName;
    roomInput.value = '';
}

async function createRoom() {
    const username = document.getElementById('username').value.trim();
    const roomName = document.getElementById('room').value.trim();
    const createRoomError = document.getElementById('createRoomError');
    const selectedRoomInput = document.getElementById('selectedRoom');

    // clear previous error messages
    createRoomError.style.display = 'none';
    createRoomError.textContent = '';

    if (!username) {
        createRoomError.textContent = 'Please enter your name.';
        createRoomError.style.display = 'block';
        return;
    }
    if (!roomName) {
        createRoomError.textContent = 'Please enter a room name to create.';
        createRoomError.style.display = 'block';
        return;
    }

    // clear selected room to prevent conflicts
    selectedRoomInput.value = '';

    try {
        const response = await fetch('/api/round/createRoom', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, roomName }),
        });

        if (response.ok) {
            localStorage.setItem('username', username);
            localStorage.setItem('roomName', roomName);
            window.location.href = '/room';
        } else {
            const error = await response.json();
            createRoomError.textContent = error.error || 'An unexpected error occurred.';
            createRoomError.style.display = 'block';
        }
    } catch (error) {
        console.error('Error:', error);
        createRoomError.textContent = 'An unexpected error occurred. Please try again.';
        createRoomError.style.display = 'block';
    }
}

async function joinRoom() {
    const username = document.getElementById('username').value.trim();
    const roomName = document.getElementById('selectedRoom').value.trim();
    const joinRoomError = document.getElementById('joinRoomError');
    const roomInput = document.getElementById('room');

    joinRoomError.style.display = 'none';
    joinRoomError.textContent = '';

    if (!username) {
        joinRoomError.textContent = 'Please enter your name.';
        joinRoomError.style.display = 'block';
        return;
    }
    if (!roomName) {
        joinRoomError.textContent = 'Please select a room to join.';
        joinRoomError.style.display = 'block';
        return;
    }

    roomInput.value = '';

    try {
        const response = await fetch('/api/round/join', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, roomName }),
        });

        if (response.ok) {
            localStorage.setItem('username', username);
            localStorage.setItem('roomName', roomName);
            window.location.href = '/room';
        } else {
            const error = await response.json();
            joinRoomError.textContent = error.error || 'An unexpected error occurred.';
            joinRoomError.style.display = 'block';
        }
    } catch (error) {
        console.error('Error:', error);
        joinRoomError.textContent = 'An unexpected error occurred. Please try again.';
        joinRoomError.style.display = 'block';
    }
}