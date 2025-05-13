// FileVault WebAssembly Demo Loader
// This file manages the loading and initialization of the WebAssembly module

const FileVaultWASM = {
    isLoaded: false,
    module: null,
    
    // Check if WebAssembly is supported in this browser
    checkSupport: function() {
        return typeof WebAssembly === 'object';
    },
    
    // Initialize the WebAssembly module
    init: async function() {
        // Update UI to show loading state
        this.updateUIState('loading');
        
        try {
            if (!this.checkSupport()) {
                throw new Error('WebAssembly is not supported in this browser');
            }
            
            // Fetch and instantiate the WebAssembly module
            try {
                const response = await fetch('/wasm/filevault.wasm');
                const buffer = await response.arrayBuffer();
                const module = await WebAssembly.instantiate(buffer, this.getImportObject());
                
                this.module = module.instance.exports;
                this.isLoaded = true;
                
                // Register event handlers
                this.registerEventHandlers();
                
                // Update UI to show ready state
                this.updateUIState('ready');
                console.log('FileVault WASM module loaded successfully');
                
                // Initialize the demo with default values
                this.initializeDemo();
            } catch (error) {
                // When the WASM module is not available, show demo mode
                console.warn('WASM module not found, falling back to demo mode:', error);
                this.startDemoMode();
            }
        } catch (error) {
            console.error('Failed to initialize WebAssembly module:', error);
            this.updateUIState('error', error.message);
        }
    },
    
    // Create the import object for the WebAssembly module
    getImportObject: function() {
        return {
            env: {
                // Memory management
                memory: new WebAssembly.Memory({ initial: 10, maximum: 100 }),
                
                // Console logging functions
                consoleLog: (ptr, len) => {
                    const bytes = new Uint8Array(this.module.memory.buffer, ptr, len);
                    const message = new TextDecoder('utf8').decode(bytes);
                    console.log('WASM log:', message);
                },
                
                // File system operations (simulated)
                createFile: (namePtr, nameLen, contentPtr, contentLen) => {
                    // In a real implementation, this would handle file operations
                    console.log('File created (simulation)');
                    return 1; // Success
                },
                
                // Time functions
                getCurrentTime: () => {
                    return Date.now();
                }
            }
        };
    },
    
    // Register event handlers for user interactions
    registerEventHandlers: function() {
        // Toggle theme button
        const themeToggle = document.getElementById('wasm-theme-toggle');
        if (themeToggle) {
            themeToggle.addEventListener('click', () => {
                this.toggleTheme();
            });
        }
        
        // File encryption button
        const encryptButton = document.getElementById('encrypt-file-btn');
        if (encryptButton) {
            encryptButton.addEventListener('click', () => {
                this.simulateFileEncryption();
            });
        }
        
        // Password input for strength indicator
        const passwordInput = document.getElementById('encryption-password');
        if (passwordInput) {
            passwordInput.addEventListener('input', (e) => {
                this.updatePasswordStrength(e.target.value);
            });
        }
        
        // File selector and drag-drop zone
        this.initializeFileUpload();
    },
    
    // Initialize file upload functionality
    initializeFileUpload: function() {
        const dropZone = document.getElementById('file-drop-zone');
        const fileInput = document.getElementById('file-input');
        
        if (dropZone && fileInput) {
            // File input change
            fileInput.addEventListener('change', (e) => {
                if (e.target.files.length > 0) {
                    this.handleFileSelection(e.target.files[0]);
                }
            });
            
            // Drag and drop events
            dropZone.addEventListener('dragover', (e) => {
                e.preventDefault();
                dropZone.classList.add('drag-over');
            });
            
            dropZone.addEventListener('dragleave', () => {
                dropZone.classList.remove('drag-over');
            });
            
            dropZone.addEventListener('drop', (e) => {
                e.preventDefault();
                dropZone.classList.remove('drag-over');
                
                if (e.dataTransfer.files.length > 0) {
                    this.handleFileSelection(e.dataTransfer.files[0]);
                }
            });
            
            // Click to select file
            dropZone.addEventListener('click', () => {
                fileInput.click();
            });
        }
    },
    
    // Handle file selection for encryption
    handleFileSelection: function(file) {
        const fileInfo = document.getElementById('selected-file-info');
        if (fileInfo) {
            fileInfo.textContent = `Selected: ${file.name} (${this.formatFileSize(file.size)})`;
            fileInfo.style.display = 'block';
        }
        
        // In a real implementation, we would process the file here
        // For now, we'll just enable the encrypt button
        const encryptButton = document.getElementById('encrypt-file-btn');
        if (encryptButton) {
            encryptButton.disabled = false;
        }
        
        // Store the file reference for later use
        this.selectedFile = file;
    },
    
    // Format file size in a human-readable way
    formatFileSize: function(bytes) {
        if (bytes < 1024) return bytes + ' bytes';
        else if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
        else return (bytes / 1048576).toFixed(1) + ' MB';
    },
    
    // Simulate file encryption process
    simulateFileEncryption: function() {
        const progressBar = document.getElementById('encryption-progress');
        const progressText = document.getElementById('encryption-status');
        const resultArea = document.getElementById('encryption-result');
        const passwordInput = document.getElementById('encryption-password');
        
        // Validate password
        const password = passwordInput ? passwordInput.value : '';
        if (!password || password.length < 8) {
            alert('Bitte geben Sie ein sicheres Passwort mit mindestens 8 Zeichen ein.');
            return;
        }
        
        if (progressBar && progressText && resultArea) {
            // Reset UI
            progressBar.style.width = '0%';
            progressBar.style.display = 'block';
            progressText.textContent = 'Starte Verschlüsselung...';
            resultArea.innerHTML = '';
            
            // Simulate encryption steps
            let progress = 0;
            const interval = setInterval(() => {
                progress += 5;
                progressBar.style.width = progress + '%';
                
                if (progress < 20) {
                    progressText.textContent = 'Initialisiere AES-256-GCM...';
                } else if (progress < 40) {
                    progressText.textContent = 'Generiere Schlüssel mit PBKDF2 (10.000 Iterationen)...';
                } else if (progress < 60) {
                    progressText.textContent = 'Verarbeite Datei...';
                } else if (progress < 80) {
                    progressText.textContent = 'Wende Verschlüsselung an...';
                } else {
                    progressText.textContent = 'Erstelle Integritäts-Header...';
                }
                
                if (progress >= 100) {
                    clearInterval(interval);
                    progressText.textContent = 'Verschlüsselung abgeschlossen!';
                    
                    // Store information for decryption demo
                    this.lastEncryptedFile = {
                        originalName: this.selectedFile ? this.selectedFile.name : 'demo.txt',
                        encryptedName: this.selectedFile ? this.selectedFile.name + '.enc' : 'demo.txt.enc',
                        size: this.selectedFile ? this.formatFileSize(this.selectedFile.size) : '24 KB',
                        password: password
                    };
                    
                    // Display the result
                    resultArea.innerHTML = `
                        <div class="success-message">
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24">
                                <path fill="currentColor" d="M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41-1.41L9,16.17z"/>
                            </svg>
                            <p>Datei erfolgreich verschlüsselt!</p>
                        </div>
                        <div class="file-details">
                            <p><strong>Originaldatei:</strong> ${this.lastEncryptedFile.originalName}</p>
                            <p><strong>Verschlüsselte Datei:</strong> ${this.lastEncryptedFile.encryptedName}</p>
                            <p><strong>Verschlüsselungsmethode:</strong> AES-256-GCM</p>
                            <p><strong>Schlüsselableitung:</strong> PBKDF2 mit 10.000 Iterationen</p>
                            <p><strong>Integrität:</strong> HMAC-SHA256</p>
                            <p><strong>Erstellungsdatum:</strong> ${new Date().toLocaleString()}</p>
                        </div>
                        <div class="button-group">
                            <button id="download-btn" class="download-button">Verschlüsselte Datei herunterladen</button>
                            <button id="decrypt-btn" class="decrypt-button">Datei entschlüsseln</button>
                        </div>
                    `;
                    
                    // Add event listeners for the buttons
                    const downloadBtn = document.getElementById('download-btn');
                    if (downloadBtn) {
                        downloadBtn.addEventListener('click', () => {
                            this.simulateFileDownload();
                        });
                    }
                    
                    const decryptBtn = document.getElementById('decrypt-btn');
                    if (decryptBtn) {
                        decryptBtn.addEventListener('click', () => {
                            this.simulateFileDecryption();
                        });
                    }
                    
                    // Add the file to the folder structure
                    this.addFileToFolderStructure();
                }
            }, 200);
        }
    },
    
    // Simulate downloading the encrypted file
    simulateFileDownload: function() {
        // Create a temporary download link
        const filename = this.lastEncryptedFile ? this.lastEncryptedFile.encryptedName : 'demo.txt.enc';
        
        // Create a blob with some dummy content
        const blob = new Blob(['This is a simulated encrypted file. In a real application, this would be encrypted data.'], { type: 'application/octet-stream' });
        const url = URL.createObjectURL(blob);
        
        // Create and click a download link
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        
        // Clean up
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    },
    
    // Simulate file decryption process
    simulateFileDecryption: function() {
        if (!this.lastEncryptedFile) {
            alert('Keine verschlüsselte Datei gefunden. Bitte verschlüsseln Sie zuerst eine Datei.');
            return;
        }
        
        // Show password dialog
        const password = prompt('Bitte geben Sie das Passwort ein, um die Datei zu entschlüsseln:', '');
        
        // Check if canceled
        if (password === null) return;
        
        // Check password
        if (password !== this.lastEncryptedFile.password) {
            alert('Falsches Passwort. Entschlüsselung fehlgeschlagen.');
            return;
        }
        
        const progressBar = document.getElementById('encryption-progress');
        const progressText = document.getElementById('encryption-status');
        const resultArea = document.getElementById('encryption-result');
        
        if (progressBar && progressText && resultArea) {
            // Reset UI
            progressBar.style.width = '0%';
            progressBar.style.display = 'block';
            progressText.textContent = 'Starte Entschlüsselung...';
            resultArea.innerHTML = '';
            
            // Simulate decryption steps
            let progress = 0;
            const interval = setInterval(() => {
                progress += 5;
                progressBar.style.width = progress + '%';
                
                if (progress < 20) {
                    progressText.textContent = 'Verifiziere Integritäts-Header...';
                } else if (progress < 40) {
                    progressText.textContent = 'Generiere Schlüssel mit PBKDF2...';
                } else if (progress < 60) {
                    progressText.textContent = 'Initialisiere AES-256-GCM...';
                } else if (progress < 80) {
                    progressText.textContent = 'Entschlüssele Daten...';
                } else {
                    progressText.textContent = 'Speichere entschlüsselte Datei...';
                }
                
                if (progress >= 100) {
                    clearInterval(interval);
                    progressText.textContent = 'Entschlüsselung abgeschlossen!';
                    
                    // Display the result
                    resultArea.innerHTML = `
                        <div class="success-message">
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24">
                                <path fill="currentColor" d="M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41-1.41L9,16.17z"/>
                            </svg>
                            <p>Datei erfolgreich entschlüsselt!</p>
                        </div>
                        <div class="file-details">
                            <p><strong>Verschlüsselte Datei:</strong> ${this.lastEncryptedFile.encryptedName}</p>
                            <p><strong>Entschlüsselte Datei:</strong> ${this.lastEncryptedFile.originalName}</p>
                            <p><strong>Integrität:</strong> Verifiziert</p>
                        </div>
                        <button id="open-btn" class="download-button">Entschlüsselte Datei öffnen</button>
                    `;
                    
                    // Add event listener for the open button
                    const openBtn = document.getElementById('open-btn');
                    if (openBtn) {
                        openBtn.addEventListener('click', () => {
                            alert('In einer echten Anwendung würde jetzt die entschlüsselte Datei geöffnet werden.');
                        });
                    }
                }
            }, 200);
        }
    },
    
    // Add the encrypted file to the folder structure visualization
    addFileToFolderStructure: function() {
        if (!this.lastEncryptedFile) return;
        
        const personalFolder = document.querySelector('.subfolder:first-of-type');
        if (personalFolder) {
            // Create new file item
            const fileItem = document.createElement('div');
            fileItem.className = 'file-item';
            fileItem.innerHTML = `
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                    <path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>
                </svg>
                <span class="item-name">${this.lastEncryptedFile.encryptedName}</span>
                <span class="item-size">${this.lastEncryptedFile.size}</span>
            `;
            
            // Add animation
            fileItem.style.animation = 'fadeIn 0.5s';
            
            // Add click handler
            fileItem.addEventListener('click', () => {
                this.simulateFileDecryption();
            });
            
            // Add to folder
            personalFolder.appendChild(fileItem);
            
            // Define animation
            const style = document.createElement('style');
            style.textContent = `
                @keyframes fadeIn {
                    from { opacity: 0; transform: translateY(-10px); }
                    to { opacity: 1; transform: translateY(0); }
                }
            `;
            document.head.appendChild(style);
        }
    },
    
    // Toggle between light and dark theme
    toggleTheme: function() {
        const demoContainer = document.getElementById('wasm-demo-container');
        if (demoContainer) {
            demoContainer.classList.toggle('dark-theme');
            
            // Update the toggle button text
            const themeToggle = document.getElementById('wasm-theme-toggle');
            if (themeToggle) {
                const isDark = demoContainer.classList.contains('dark-theme');
                themeToggle.textContent = isDark ? '☀️ Light Mode' : '🌙 Dark Mode';
            }
        }
    },
    
    // Update the UI state based on the loading status
    updateUIState: function(state, message) {
        const container = document.getElementById('wasm-demo-container');
        const loadingIndicator = document.getElementById('wasm-loading');
        const demoContent = document.getElementById('wasm-content');
        const errorMessage = document.getElementById('wasm-error');
        
        if (container && loadingIndicator && demoContent && errorMessage) {
            // Reset all states
            loadingIndicator.style.display = 'none';
            demoContent.style.display = 'none';
            errorMessage.style.display = 'none';
            
            // Set the appropriate state
            switch (state) {
                case 'loading':
                    loadingIndicator.style.display = 'flex';
                    break;
                    
                case 'ready':
                    demoContent.style.display = 'block';
                    break;
                    
                case 'error':
                    errorMessage.style.display = 'block';
                    errorMessage.querySelector('p').textContent = message || 'Failed to load WebAssembly module';
                    break;
            }
        }
    },
    
    // Initialize the demo with default values
    initializeDemo: function() {
        // In a real implementation, this would initialize the WASM module
        // For now, we'll just use the simulation functions
        
        // Set default theme based on system preference
        const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
        const demoContainer = document.getElementById('wasm-demo-container');
        
        if (demoContainer && prefersDark) {
            demoContainer.classList.add('dark-theme');
            
            // Update toggle button text
            const themeToggle = document.getElementById('wasm-theme-toggle');
            if (themeToggle) {
                themeToggle.textContent = '☀️ Light Mode';
            }
        }
    },
    
    // Start demo mode when WASM module is not available
    startDemoMode: function() {
        console.log('Starting FileVault demo mode (WASM not available)');
        this.isLoaded = true; // Pretend we loaded successfully
        
        // Register event handlers
        this.registerEventHandlers();
        
        // Update UI to show ready state
        this.updateUIState('ready');
        
        // Initialize the demo with default values
        this.initializeDemo();
    },
    
    // Update password strength indicator
    updatePasswordStrength: function(password) {
        const strengthBar = document.querySelector('.strength-bar');
        if (!strengthBar) return;
        
        // Reset classes
        strengthBar.classList.remove('weak', 'medium', 'strong');
        
        if (!password) {
            strengthBar.style.width = '0';
            return;
        }
        
        // Simple password strength algorithm
        let strength = 0;
        
        // Length check
        if (password.length >= 8) strength += 1;
        if (password.length >= 12) strength += 1;
        
        // Character diversity
        if (/[A-Z]/.test(password)) strength += 1;  // Uppercase
        if (/[a-z]/.test(password)) strength += 1;  // Lowercase
        if (/[0-9]/.test(password)) strength += 1;  // Numbers
        if (/[^A-Za-z0-9]/.test(password)) strength += 1;  // Special characters
        
        // Calculate strength level
        if (strength <= 2) {
            strengthBar.classList.add('weak');
        } else if (strength <= 4) {
            strengthBar.classList.add('medium');
        } else {
            strengthBar.classList.add('strong');
        }
    }
};

// Initialize the WASM module when the page loads
document.addEventListener('DOMContentLoaded', () => {
    FileVaultWASM.init();
}); 