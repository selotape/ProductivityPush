// Content script for additional blocking logic and motivation
(function() {
    'use strict';

    // Check if current site should be blocked
    function checkBlocking() {
        const hostname = window.location.hostname;

        chrome.storage.local.get(['blockingEnabled', 'blockedSites'], (result) => {
            if (!result.blockingEnabled) return;

            const blockedSites = result.blockedSites || [];
            const isBlocked = blockedSites.some(site => hostname.includes(site));

            if (isBlocked) {
                showBlockedPage();
            }
        });
    }

    // Show blocked page overlay
    function showBlockedPage() {
        document.body.innerHTML = `
            <div style="
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                display: flex;
                align-items: center;
                justify-content: center;
                z-index: 9999;
                font-family: Arial, sans-serif;
                color: white;
            ">
                <div style="text-align: center; max-width: 600px; padding: 40px;">
                    <h1 style="font-size: 48px; margin-bottom: 20px;">🚫</h1>
                    <h2 style="font-size: 32px; margin-bottom: 20px;">Site Blocked</h2>
                    <p style="font-size: 18px; margin-bottom: 30px;">
                        This site is blocked by ProductivityPush to help you stay focused.
                    </p>
                    <div style="background: rgba(255,255,255,0.1); padding: 20px; border-radius: 10px; margin-bottom: 30px;">
                        <p style="margin: 0; font-size: 16px;">
                            💡 Why not use this time to complete a productive task instead?
                        </p>
                    </div>
                    <button onclick="window.close()" style="
                        background: white;
                        color: #667eea;
                        border: none;
                        padding: 12px 24px;
                        border-radius: 25px;
                        font-size: 16px;
                        cursor: pointer;
                        font-weight: bold;
                    ">Close Tab</button>
                </div>
            </div>
        `;
    }

    // Run blocking check when page loads
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', checkBlocking);
    } else {
        checkBlocking();
    }
})();