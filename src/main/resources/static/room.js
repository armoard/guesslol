let stompClient = null;
const username = localStorage.getItem('username');
const roomName = localStorage.getItem('roomName');
let currentRound = {
    champions: [],
    currentChampionIndex: 0,
    currentSkillsShowed: 0,
    players: []
};
const timerElement = document.getElementById('time')

let timer;
let timeLeft = 25;
let roundStarted = false;

if (!username || !roomName) {
    window.location.href = '/';
}

function connectToChat() {
    const username = localStorage.getItem('username');
    const roomName = localStorage.getItem('roomName');
    const socket = new WebSocket(`wss://guesslol.com/chat?username=${username}&roomName=${roomName}`);
    stompClient = Stomp.over(socket);
    stompClient.debug = () => {};
    stompClient.connect({}, function () {
        handleUserJoined(username);
        stompClient.subscribe('/topic/chat/' + roomName, async function (message) {
            const receivedMessage = JSON.parse(message.body);
            switch (receivedMessage.type) {
                case 'user_joined':
                    handleUserJoined(receivedMessage.username);
                    break;

                case 'user_left':
                    handleUserLeft(receivedMessage.username);
                    break;

                case 'round_starting':
                    handleRoundStarting(receivedMessage);
                    break;

                case 'chat_message':
                    handleChatMessage(receivedMessage.username, receivedMessage.content);
                    break;

                case 'advance':
                    handleAdvanceChampion(receivedMessage);
                    break;

                case 'round_info':
                    handleRoundInfo(receivedMessage.additionalData);
                    break;

                case 'round_terminated':
                    handleRoundTermination(receivedMessage);
                    break;

                default:
                    break;
            }
        });
    }, function (error) {
        console.error("Error connecting to WebSocket:", error);
    });
}

function handleUserJoined(username) {
    const playerExists = currentRound.players.some(player => player.username === username);

    if (!playerExists) {
        currentRound.players.push({ username, score: 0 });
        updateWaitingRoomPlayers(currentRound.players);
    }

    handleSystemMessage(`${username} has joined the room.`, "user_joined");
}

function handleUserLeft(username) {
    currentRound.players = currentRound.players.filter(player => player.username !== username);

    if (!roundStarted) {
        updateWaitingRoomPlayers(currentRound.players);
    } else {
        updatePlayerScores(currentRound.players);
    }

    handleSystemMessage(`${username} has left the room.`, "user_left");
}

function handleChatMessage(username, content) {
    const chatMessages = document.getElementById('chat-messages');
    const newMessage = document.createElement('div');
    newMessage.textContent = `${username}: ${content}`;
    chatMessages.appendChild(newMessage);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function handleSystemMessage(content, type = "info") {
    const chatMessages = document.getElementById('chat-messages');

    const newMessage = document.createElement('div');
    newMessage.textContent = content;


    switch (type) {
        case "winner":
            newMessage.style.color = '#28a745';
            newMessage.style.fontWeight = 'bold';
            break;
        case "round_terminated":
            newMessage.style.color = '#dc3545';
            newMessage.style.fontWeight = 'bold';
            break;
        case "user_joined":
            newMessage.style.color = '#17a2b8';
            break;
        case "user_left":
            newMessage.style.color = '#ffc107';
            break;
        case "correct_answer":
            newMessage.style.color = '#007bff';
            newMessage.style.fontStyle = 'italic';
            break;
        default:
            newMessage.style.color = '#6c757d';
            break;
    }

    chatMessages.appendChild(newMessage);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}


function handleAdvanceChampion(message) {
    const { username } = message;
    const currentChampion = currentRound.champions[currentRound.currentChampionIndex];

    handleSystemMessage(`${username} guessed! The champion was ${currentChampion.name}.`, "correct_answer");

    const player = currentRound.players.find(p => p.username === username);
    if (player) {
        player.score += 1;
        updatePlayerScores(currentRound.players);
    }

    if (currentRound.currentChampionIndex < currentRound.champions.length - 1) {
        currentRound.currentChampionIndex++;
        currentRound.currentSkillsShowed = 0;
        startLocalTimer();
        updateChampionDisplay(timeLeft);
    } else {

        if (timer) {
            clearInterval(timer);
            timer = null;
        }

        fetch(`/api/round/terminate/${roomName}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        })
        .then(response => {
            if (!response.ok) throw new Error("Error terminating the round.");
        })
        .catch(error => console.error("Error terminating the round.", error));
    }
}

function handleRoundTermination(message) {
    const gameContainer = document.getElementById('game-container');
    const waitingRoom = document.getElementById('waiting-room');
    const startRoundBtn = document.getElementById('start-round-btn');

    if (message.content) {
        handleSystemMessage(message.content, "winner");
        handleSystemMessage("The round has ended, returning to waiting room...", "round_terminated");
    } else {
        handleSystemMessage("The round has ended!", "round_terminated");
    }

    setTimeout(() => {
        if (gameContainer) gameContainer.classList.add('hidden');
        if (waitingRoom) waitingRoom.classList.remove('hidden');

        if (startRoundBtn) {
            startRoundBtn.classList.remove('hidden');
            startRoundBtn.disabled = false;
            startRoundBtn.replaceWith(startRoundBtn.cloneNode(true));
            const newStartRoundBtn = document.getElementById('start-round-btn');
            newStartRoundBtn.addEventListener('click', () => {
                startRound();
            });
        } else {
            console.warn("Start round button not found.");
        }

        document.getElementById('championImage').src = '';
        document.querySelectorAll('#abilities img').forEach(ability => {
            ability.src = '/ChampionSquare.png';
        });
        document.getElementById('time').innerText = "Remaining time: 25 seconds";
    }, 5000);
}

function sendMessage() {
    const messageInput = document.getElementById('messageInput');
    const messageContent = messageInput.value.trim();

    if (messageContent && stompClient) {
        validateAnswer(messageContent);

        const message = {
            type: "chat_message",
            username: localStorage.getItem('username'),
            roomName: localStorage.getItem('roomName'),
            content: messageContent
        };
        stompClient.send("/app/chat/message", {}, JSON.stringify(message));
        messageInput.value = "";
    } else {
        console.warn("Cannot send an empty or offline message..");
    }
}

function startRound() {
    const roomName = localStorage.getItem('roomName');

    fetch(`/api/round/start/${roomName}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`Error al iniciar la ronda: ${response.statusText}`);
        }
    })
    .catch(error => {
        console.error("Error starting the round", error.message);
    });
}

function handleRoundStarting(message) {
    const startingMessage = document.getElementById('round-starting-message');
    const startRoundBtn = document.getElementById('start-round-btn');

    if (startingMessage) {
        startingMessage.classList.remove('hidden');
    }

    if (startRoundBtn) {
        startRoundBtn.disabled = true;
        startRoundBtn.classList.add('hidden');
    }
}

function handleRoundInfo(data) {
    const startingMessage = document.getElementById('round-starting-message');
    if (startingMessage) {
        startingMessage.classList.add('hidden');
    }

    const waitingRoom = document.getElementById('waiting-room');
    const gameContainer = document.getElementById('game-container');

    if (waitingRoom) waitingRoom.classList.add('hidden');
    if (gameContainer) gameContainer.classList.remove('hidden');

    const roundData = data.round;
    currentRound = {
        champions: roundData.champions || [],
        currentChampionIndex: 0,
        currentSkillsShowed: 0,
        players: roundData.players || []
    };

    roundStarted = true;
    startLocalTimer();
    updateChampionDisplay(timeLeft);
    updatePlayerScores(currentRound.players);

}

function startLocalTimer() {
    timeLeft = 25;
    updateTimerDisplay();

    if (timer) {
        clearInterval(timer);
    }

    timer = setInterval(() => {
        timeLeft--;

        setTimeout(() => {
            updateTimerDisplay();
            updateChampionDisplay(timeLeft);
        }, 250);

        if (timeLeft <= 0) {
            clearInterval(timer);
            const currentChampion = currentRound.champions[currentRound.currentChampionIndex];
            handleSystemMessage(`Time's up! The champion was ${currentChampion.name}.`);

            if (currentRound.currentChampionIndex < currentRound.champions.length - 1) {
                currentRound.currentChampionIndex++;
                currentRound.currentSkillsShowed = 0;


                setTimeout(() => {
                    startLocalTimer();
                    updateChampionDisplay(timeLeft);
                }, 500);
            } else {
                handleSystemMessage("The round has ended!");
            }
        }
    }, 1000);
}


function updateTimerDisplay() {
    timerElement.innerText = `Remaining time: ${timeLeft} seconds`;
}


function updateChampionDisplay(timeLeft) {
    const champion = currentRound.champions[currentRound.currentChampionIndex];

    const championImageElement = document.getElementById('championImage');
    if (championImageElement) {
        championImageElement.src = champion.championImage;
        championImageElement.style.display = 'block';
    } else {
        console.error("Element with ID championImage does not exist in the DOM");
    }

    setTimeout(() => {
        const abilities = [
            document.getElementById('ability1'),
            document.getElementById('ability2'),
            document.getElementById('ability3'),
            document.getElementById('ability4')
        ];
        const skillImages = [
            champion.spellImages.Q,
            champion.spellImages.W,
            champion.spellImages.E,
            champion.spellImages.R
        ];

        abilities.forEach(ability => {
            ability.src = '/ChampionSquare.png';
            ability.style.display = 'block';
        });

        const skillsToShow = Math.floor((25 - timeLeft) / 5); // how many abilities we need to show

        for (let i = 0; i < skillsToShow; i++) {
            if (abilities[i]) {
                abilities[i].src = skillImages[i];
            }
        }
    }, 500);
}


function validateAnswer(answer) {
    if (!roundStarted) {
        console.warn("The round has already ended. No further answers are accepted.");
        return;
    }

    if (!currentRound.champions || currentRound.champions.length === 0) {
        console.error("No champions are available to validate.");
        return;
    }

    if (currentRound.currentChampionIndex < 0 || currentRound.currentChampionIndex >= currentRound.champions.length) {
        console.error("Invalid current champion index.");
        return;
    }

    const currentChampion = currentRound.champions[currentRound.currentChampionIndex];
    const currentChampionName = currentChampion.name;

    if (!currentChampionName) {
        console.error("The current champion's name was not found.");
        return;
    }

    if (answer.toLowerCase() === currentChampionName.toLowerCase()) {
        fetch(`/api/round/advance`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                roomName: roomName,
                username: username
            })
        })
       .catch(() => {
           console.log("Failed to notify the backend to advance.");
       });
    }
}

function updateWaitingRoomPlayers(players) {
    const waitingRoomList = document.getElementById('waiting-room-players');
    if (!waitingRoomList) {
        console.error("Element with ID 'waiting-room-players' not found.");
        return;
    }

    waitingRoomList.innerHTML = '';

    players.forEach(player => {
        const playerItem = document.createElement('li');
        playerItem.textContent = player.username;
        waitingRoomList.appendChild(playerItem);
    });

}

function updatePlayerScores(players) {
    const scoresContainer = document.getElementById('scoreboard-players');
    scoresContainer.innerHTML = '';

    players.forEach((player, index) => {
        const playerScore = document.createElement('li');
        playerScore.innerHTML = `
            <span class="name">${player.username}</span>
            <span class="score">${player.score}</span>
        `;
        scoresContainer.appendChild(playerScore);
    });
}


document.addEventListener('DOMContentLoaded', async () => {
    connectToChat();

    const messageInput = document.getElementById('messageInput');
    if (messageInput) {
        messageInput.addEventListener('keydown', function(event) {
            if (event.key === "Enter") {
                sendMessage();
            }
        });
    } else {
        console.error("Input with ID 'messageInput' not found.");
    }

    // fetch initial players in the room
    try {
        const response = await fetch(`/api/round/players/${roomName}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' },
        });

        if (response.ok) {
            const data = await response.json();
            currentRound.players = data.data;
            updateWaitingRoomPlayers(data.data);
        } else {
            console.error('Failed to fetch players:', response.statusText);
        }
    } catch (error) {
        console.error('Error fetching players:', error);
    }
});