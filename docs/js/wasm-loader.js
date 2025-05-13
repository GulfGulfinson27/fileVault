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
        
        if (progressBar && progressText && resultArea) {
            // Reset UI
            progressBar.style.width = '0%';
            progressBar.style.display = 'block';
            progressText.textContent = 'Starting encryption...';
            resultArea.innerHTML = '';
            
            // Simulate encryption steps
            let progress = 0;
            const interval = setInterval(() => {
                progress += 5;
                progressBar.style.width = progress + '%';
                
                if (progress < 30) {
                    progressText.textContent = 'Initializing encryption...';
                } else if (progress < 60) {
                    progressText.textContent = 'Processing file...';
                } else if (progress < 90) {
                    progressText.textContent = 'Applying AES-256 encryption...';
                } else {
                    progressText.textContent = 'Finalizing...';
                }
                
                if (progress >= 100) {
                    clearInterval(interval);
                    progressText.textContent = 'Encryption complete!';
                    
                    // Display the result
                    resultArea.innerHTML = `
                        <div class="success-message">
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24">
                                <path fill="currentColor" d="M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41-1.41L9,16.17z"/>
                            </svg>
                            <p>File encrypted successfully!</p>
                        </div>
                        <div class="file-details">
                            <p><strong>Original file:</strong> ${this.selectedFile ? this.selectedFile.name : 'demo.txt'}</p>
                            <p><strong>Encrypted file:</strong> ${this.selectedFile ? this.selectedFile.name + '.enc' : 'demo.txt.enc'}</p>
                            <p><strong>Encryption method:</strong> AES-256-GCM</p>
                            <p><strong>Key derivation:</strong> PBKDF2 with 10,000 iterations</p>
                        </div>
                        <button id="download-btn" class="download-button">Download Encrypted File</button>
                    `;
                    
                    // Add event listener for the download button
                    const downloadBtn = document.getElementById('download-btn');
                    if (downloadBtn) {
                        downloadBtn.addEventListener('click', () => {
                            alert('In a real implementation, this would download the encrypted file.');
                        });
                    }
                }
            }, 200);
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
    }
};

// Initialize the WASM module when the page loads
document.addEventListener('DOMContentLoaded', () => {
    FileVaultWASM.init();
}); 