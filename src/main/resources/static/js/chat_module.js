const ChatModule = {
    data: {
        currentChannelId: 1,
        chatRooms: [],
        currentUserId: null,
        currentUsername: null,
        currentChannelSubscription: null,
        processedMessageIds: new Set(),
        stompClient: null
    },

    ui: {
        scrollToBottom: () => {
            const chatMessages = document.getElementById('chatMessages');
            setTimeout(() => {
                chatMessages.scrollTop = chatMessages.scrollHeight;
            }, 0);
        },

        renderChatRooms: (channels) => {
            const channelList = document.getElementById('channelList');
            channelList.innerHTML = '';

            if (!channels || channels.length === 0) {
                channelList.innerHTML = `
                    <div class="text-center p-4 text-gray-400">
                        <span>No channel available</span>
                    </div>
                `;
                return;
            }

            channels.forEach(channel => {
                if (!channel || !channel.channelName) {
                    console.error('Invalid channel data:', channel);
                    return;
                }

                const isActive = channel.channelId === ChatModule.data.currentChannelId;
                const initials = channel.channelName ? channel.channelName.split(' ').map(n => n[0]).join('').toUpperCase() : '?';

                const channelElement = document.createElement('div');
                channelElement.className = isActive
                    ? 'bg-discord-700 rounded p-2 flex items-center gap-3 mb-1 cursor-pointer'
                    : 'hover:bg-discord-700/50 rounded p-2 flex items-center gap-3 mb-1 cursor-pointer';
                channelElement.innerHTML = `
                    <div class="relative">
                        <div class="w-8 h-8 rounded-full bg-${ChatModule.utils.getRandomColor(channel.channelId)} flex items-center justify-center">
                            <span class="text-xs font-medium">${initials}</span>
                        </div>
                        <span class="absolute bottom-0 right-0 w-3 h-3 bg-green-500 rounded-full border-2 border-discord-800"></span>
                    </div>
                    <div class="flex-grow min-w-0">
                        <p class="font-medium truncate">${ChatModule.utils.escapeHTML(channel.channelName)}</p>
                        <p class="text-xs text-discord-400 truncate">${channel.description || 'No description'}</p>
                    </div>
                `;

                channelElement.addEventListener('click', () => ChatModule.actions.switchChatRoom(channel.channelId));
                channelList.appendChild(channelElement);

            });
        },

        updateChatHeader: (channelId) => {
            const channel = ChatModule.data.chatRooms.find(c => c.channelId === channelId);
            if (!channel) return;

            const initials = channel.channelName.split(' ').map(n => n[0]).join('').toUpperCase();
            const headerHTML = `
        <div class="flex items-center gap-2">
            <div class="relative">
                <div class="w-10 h-10 rounded-full bg-${ChatModule.utils.getRandomColor(channel.channelId)} flex items-center justify-center">
                    <span class="font-medium">${initials}</span>
                </div>
                <span class="absolute bottom-0 right-0 w-3 h-3 bg-green-500 rounded-full border-2 border-discord-700"></span>
            </div>

            <div>
                <h2 id="channelHeader" class="font-semibold">${ChatModule.utils.escapeHTML(channel.channelName)}</h2>
                <p id="channelHeaderDescription" class="text-xs text-discord-400">${channel.description || 'No description'}</p>
            </div>
        </div>
    `;

            document.querySelector('.bg-discord-700.border-b.border-discord-900.p-4.flex.items-center.justify-between').firstElementChild.innerHTML = headerHTML;
        },

        addMessage: (message, isMyMessage) => {
            const content = message.messageContent;
            if (!content) {
                console.error("Message has no content:", message);
                return;
            }

            const isSystemMessage = message.userId === 0 ||
                message.username === "System Notification" ||
                message.isSystemMessage === true;

            if (isSystemMessage) {
                isMyMessage = false;
            } else {
                const msgUserId = parseInt(message.userId, 10);
                const calculatedIsMyMessage = msgUserId === ChatModule.data.currentUserId;
                if (isMyMessage !== calculatedIsMyMessage) {
                    console.warn("isMyMessage mismatch! Fixing...");
                    isMyMessage = calculatedIsMyMessage;
                }
            }

            const timestamp = message.messageCreatedAt || message.timestamp || new Date();
            const time = new Date(timestamp).toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});

            const displayName = isSystemMessage ? message.username : (isMyMessage ? 'You' : message.username);

            const avatarBg = isSystemMessage ? 'bg-yellow-600' : (isMyMessage ? 'bg-green-600' : 'bg-primary-600');
            const avatarInitials = isSystemMessage ? 'SYS' : (isMyMessage ? 'ME' : message.username.split(' ').map(n => n[0]).join('').toUpperCase());

            const messageBg = isSystemMessage ? 'bg-yellow-600/30' : (isMyMessage ? 'bg-green-600/30' : 'bg-primary-600/30');

            let messageHTML = '';

            if (isMyMessage) {
                messageHTML = `
        <div class="flex flex-row-reverse gap-4 message-hover group animate__animated animate__fadeIn">
            <div class="flex-shrink-0 mt-1">
                <div class="w-10 h-10 rounded-full ${avatarBg} flex items-center justify-center">
                    <span class="font-medium">${avatarInitials}</span>
                </div>
            </div>

            <div class="flex-grow text-right">
                <div class="flex items-baseline justify-end">
                    <span class="mr-2 text-xs text-discord-400">${time}</span>
                    <h4 class="font-medium">${displayName}</h4>
                </div>

                <div class="mt-1 text-discord-200 bg-green-600/30 p-2 rounded-lg inline-block ml-auto">
                    <p>${ChatModule.utils.escapeHTML(content)}</p>
                </div>

                <div class="message-actions opacity-0 group-hover:opacity-100 transition-opacity flex gap-2 mt-1 justify-end">
                    <button class="text-discord-400 hover:text-white">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                        </svg>
                    </button>
                    <button class="text-discord-400 hover:text-white">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                    </button>
                    <button class="text-discord-400 hover:text-white">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
                        </svg>
                    </button>
                </div>
            </div>
        </div>`;
            } else if (isSystemMessage || message.username == 'system') {
                messageHTML = `
    <div class="flex gap-4 message-hover group animate__animated animate__fadeIn">
        <div class="flex-shrink-0 mt-1">
            <div class="w-10 h-10 rounded-full bg-yellow-600 flex items-center justify-center">
                <span class="font-medium">SYS</span>
            </div>
        </div>

        <div class="flex-grow">
            <div class="flex items-baseline">
                <h4 class="font-medium text-yellow-500">${displayName}</h4>
                <span class="ml-2 text-xs text-discord-400">${time}</span>
            </div>

            <div class="mt-1 text-discord-200 bg-yellow-600/30 p-2 rounded-lg inline-block border border-yellow-500/30">
                <p>${ChatModule.utils.escapeHTML(content)}</p>
            </div>
        </div>
    </div>`;
            } else {
                messageHTML = `
        <div class="flex gap-4 message-hover group animate__animated animate__fadeIn">
            <div class="flex-shrink-0 mt-1">
                <div class="w-10 h-10 rounded-full ${avatarBg} flex items-center justify-center">
                    <span class="font-medium">${avatarInitials}</span>
                </div>
            </div>

            <div class="flex-grow">
                <div class="flex items-baseline">
                    <h4 class="font-medium">${displayName}</h4>
                    <span class="ml-2 text-xs text-discord-400">${time}</span>
                </div>

                <div class="mt-1 text-discord-200 bg-primary-600/30 p-2 rounded-lg inline-block">
                    <p>${ChatModule.utils.escapeHTML(content)}</p>
                </div>
                <div class="message-actions opacity-0 group-hover:opacity-100 transition-opacity flex gap-2 mt-1">
                    <button class="text-discord-400 hover:text-white">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5" />
                        </svg>
                    </button>
                    <button class="text-discord-400 hover:text-white">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                    </button>
                    <button class="text-discord-400 hover:text-white">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
                        </svg>
                    </button>
                </div>
            </div>
        </div>`;
            }

            document.getElementById('chatMessages').insertAdjacentHTML('beforeend', messageHTML);
            ChatModule.ui.scrollToBottom();
        }
    },

    utils: {
        getRandomColor: (id) => {
            const colors = ['primary', 'purple', 'green', 'red', 'amber', 'indigo', 'pink'];
            return colors[id % colors.length];
        },

        escapeHTML: (str) => {
            return str.replace(/[&<>'"]/g,
                tag => ({
                    '&': '&amp;',
                    '<': '&lt;',
                    '>': '&gt;',
                    "'": '&#39;',
                    '"': '&quot;'
                }[tag]));
        },

        formatTime: (date) => {
            return date.toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
        },

        isSystemChannel: (channelName) => {
            return channelName === "System";
        },
    },

    actions: {
        sendMessage: () => {
            const messageInput = document.getElementById('messageInput');
            const message = messageInput.value.trim();

            const currentChannel = ChatModule.data.chatRooms.find(c => c.channelId === ChatModule.data.currentChannelId);

            if (currentChannel && ChatModule.utils.isSystemChannel(currentChannel.channelName)) {
                messageInput.value = '';
                return;
            }

            if (message) {
                const clientMessageId = `${ChatModule.data.currentUserId}-${new Date().getTime()}`;

                const chatMessage = {
                    messageContent: message,
                    channelId: ChatModule.data.currentChannelId,
                    messageCreatedAt: new Date(),
                    clientMessageId: clientMessageId
                };

                const messageKey = `${message}-${Date.now()}`;
                if (ChatModule.data.sentMessages.has(messageKey)) {
                    return;
                }

                ChatModule.data.sentMessages.add(messageKey);
                setTimeout(() => ChatModule.data.sentMessages.delete(messageKey), 5000);

                ChatModule.data.stompClient.send(`/app/chat.sendMessage/${ChatModule.data.currentChannelId}`, {}, JSON.stringify(chatMessage));

                messageInput.value = '';
                messageInput.style.height = 'auto';
                messageInput.focus();

                ChatModule.ui.scrollToBottom();
            }
        },
        switchChatRoom: (channelId) => {
            if (channelId === ChatModule.data.currentChannelId) return;

            if (ChatModule.data.currentChannelSubscription) {
                ChatModule.data.currentChannelSubscription.unsubscribe();
            }

            ChatModule.data.currentChannelId = channelId;

            ChatModule.ui.renderChatRooms(ChatModule.data.chatRooms);
            ChatModule.ui.updateChatHeader(channelId);

            document.getElementById('chatMessages').innerHTML = '';
            document.getElementById('chatMessages').innerHTML = `
        <div class="flex justify-center p-4">
            <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
            </div>
        </div>
    `;

            const currentChannel = ChatModule.data.chatRooms.find(c => c.channelId === channelId);

            const isSystemChannel = currentChannel && ChatModule.utils.isSystemChannel(currentChannel.channelName);

            const messageInput = document.getElementById('messageInput');
            const sendButton = document.querySelector('#chatForm button[type="submit"]');

            if (isSystemChannel) {
                messageInput.disabled = true;
                messageInput.placeholder = "You cannot send messages in system channels";
                sendButton.disabled = true;
                sendButton.classList.add('opacity-50', 'cursor-not-allowed');
            } else {
                messageInput.disabled = false;
                messageInput.placeholder = "Type a message...";
                sendButton.disabled = false;
                sendButton.classList.remove('opacity-50', 'cursor-not-allowed');
            }

            ChatModule.api.loadChatMessages(channelId);
            ChatModule.websocket.subscribeToChatRoom(channelId);
        },
    },

    api: {
        loadChatRooms: () => {
            fetch('/api/chat/getAllChannels')
                .then(response => response.json())
                .then(data => {
                    ChatModule.data.chatRooms = data;
                    ChatModule.ui.renderChatRooms(data);
                })
                .catch(error => {
                    console.error('Error loading chat rooms:', error);
                    document.getElementById('channelList').innerHTML = `
                        <div class="text-center p-4 text-red-400">
                            <span>Failed to load chats. Please try again.</span>
                        </div>
                    `;
                });
        },

        loadChatMessages: (channelId) => {
            fetch(`/api/chat/channels/${channelId}/messages?page=0&size=20`)
                .then(response => response.json())
                .then(data => {
                    document.getElementById('chatMessages').innerHTML = '';

                    const dateDiv = document.createElement('div');
                    dateDiv.className = 'message-divider';
                    dateDiv.innerHTML = `<span class="bg-discord-700 text-discord-400 text-xs px-2">Today</span>`;
                    document.getElementById('chatMessages').appendChild(dateDiv);

                    const systemMsg = document.createElement('div');
                    systemMsg.className = 'flex justify-center';
                    systemMsg.innerHTML = `
                        <div class="bg-discord-800 text-discord-300 text-xs px-3 py-1 rounded-full">
                            Chat started
                        </div>
                    `;
                    document.getElementById('chatMessages').appendChild(systemMsg);

                    data.reverse();

                    data.forEach(msg => {
                        const msgUserId = parseInt(msg.userId, 10);
                        const isMyMessage = msgUserId === ChatModule.data.currentUserId;
                        msg.userId = msgUserId;
                        ChatModule.ui.addMessage(msg, isMyMessage);
                    });

                    ChatModule.ui.scrollToBottom();
                })
                .catch(error => {
                    console.error('Error loading messages:', error);
                    document.getElementById('chatMessages').innerHTML = `
                        <div class="text-center p-4 text-red-400">
                            <span>Failed to load messages. Please try again.</span>
                        </div>
                    `;
                });
        }
    },

    websocket: {
        connect: () => {
            const socket = new SockJS('/ws');
            ChatModule.data.stompClient = Stomp.over(socket);

            ChatModule.data.stompClient.connect({}, function () {
                ChatModule.websocket.subscribeToChatRoom(ChatModule.data.currentChannelId);

                ChatModule.data.stompClient.subscribe('/topic/test', function (response) {
                    const sessionInfo = response.body;
                    const userIdMatch = sessionInfo.match(/userId=(\d+)/);
                    const usernameMatch = sessionInfo.match(/username=([^,]+)/);

                    if (userIdMatch && usernameMatch) {
                        const wsUserId = parseInt(userIdMatch[1], 10);
                        const wsUsername = usernameMatch[1];

                        if (!ChatModule.data.currentUserId) {
                            ChatModule.data.currentUserId = wsUserId;
                            ChatModule.data.currentUsername = wsUsername;
                        } else if (ChatModule.data.currentUserId !== wsUserId) {
                            console.warn("User ID mismatch! API:", ChatModule.data.currentUserId, "WebSocket:", wsUserId);
                            ChatModule.data.currentUserId = wsUserId;
                            ChatModule.data.currentUsername = wsUsername;
                        }

                        ChatModule.api.loadChatRooms();
                        ChatModule.init.setupChatForm();
                    }
                });

                ChatModule.data.stompClient.send("/app/test.checkSession", {}, "Check my session");
            });
        },
        subscribeToChatRoom: (channelId) => {
            if (ChatModule.data.currentChannelSubscription) {
                ChatModule.data.currentChannelSubscription.unsubscribe();
            }

            ChatModule.api.loadChatMessages(channelId);

            ChatModule.data.currentChannelSubscription = ChatModule.data.stompClient.subscribe(`/topic/chat/${channelId}`, function (message) {
                try {
                    const messageData = JSON.parse(message.body);

                    const isSystemMessage = messageData.userId === 0 || messageData.username === "System Notification";

                    const isMyMessage = !isSystemMessage && parseInt(messageData.userId, 10) === ChatModule.data.currentUserId;

                    if (messageData.channelId === ChatModule.data.currentChannelId) {
                        ChatModule.ui.addMessage(messageData, isMyMessage);
                    }
                } catch (error) {
                    console.error("Error processing message:", error);
                }
            });
        }

    },

    init: {
        setupChatForm: () => {
            const chatForm = document.getElementById('chatForm');
            const messageInput = document.getElementById('messageInput');

            ChatModule.data.sentMessages = new Set();

            chatForm.addEventListener('submit', function (e) {
                e.preventDefault();
                ChatModule.actions.sendMessage();
            });

            messageInput.addEventListener('keydown', function (e) {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    ChatModule.actions.sendMessage();
                }
            });
        },

        setupTextareaAutosize: () => {
            const messageInput = document.getElementById('messageInput');
            messageInput.addEventListener('input', function () {
                this.style.height = 'auto';
                this.style.height = (this.scrollHeight) + 'px';
                if (this.scrollHeight > 150) {
                    this.style.height = '150px';
                    this.style.overflowY = 'auto';
                } else {
                    this.style.overflowY = 'hidden';
                }
            });
        },

        initialize: () => {
            ChatModule.data.processedMessageIds = new Set();
            if (typeof currentUserInfo !== 'undefined') {
                ChatModule.data.currentUserId = parseInt(currentUserInfo.userId, 10);
                ChatModule.data.currentUsername = currentUserInfo.username;
            } else {
                console.error("User info not available");
                document.getElementById('chatMessages').innerHTML = `
                    <div class="text-center p-4 text-red-400">
                        <span>Failed to identify user. Please refresh the page.</span>
                    </div>
                `;
            }

            ChatModule.websocket.connect();

            ChatModule.init.setupTextareaAutosize();

            document.getElementById('messageInput').focus();
        }
    }
};

document.addEventListener('DOMContentLoaded', ChatModule.init.initialize);
