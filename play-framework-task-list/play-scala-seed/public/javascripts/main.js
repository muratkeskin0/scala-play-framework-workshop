// ========================================
// MODERN AJAX TASK MANAGEMENT SYSTEM
// ========================================

// Global variables for AJAX functionality
let currentTasks = [];
let isLoading = false;

// Get CSRF token from cookie (more secure)
function getCsrfToken() {
    const name = "PLAY_CSRF_TOKEN=";
    const decodedCookie = decodeURIComponent(document.cookie);
    const ca = decodedCookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

// Initialize the modern task system
$(document).ready(function() {
    console.log('🚀 Initializing Modern Task Management System...');
    
    // Load tasks when page loads
    loadTasks();
    
    // Setup AJAX form handlers
    setupAjaxHandlers();
    
    // Setup real-time notifications
    setupNotifications();
    
    // Setup view toggle handlers
    setupViewToggle();
    
    console.log('✅ Modern Task Management System initialized!');
});

// ========================================
// AJAX TASK OPERATIONS
// ========================================

// Load all tasks via AJAX (silent - no notifications)
function loadTasks() {
    loadTasksWithNotification(false);
}

// Load all tasks via AJAX (with notification)
function loadTasksWithNotification(showNotification = true) {
    if (isLoading) return;
    
    isLoading = true;
    showLoadingSpinner();
    
    $.ajax({
        url: '/api/tasks',
        method: 'GET',
        dataType: 'json',
        headers: {
            'X-CSRF-Token': getCsrfToken()
        },
        success: function(response) {
            if (response.success) {
                currentTasks = response.tasks;
                renderTasks(response.tasks);
                if (showNotification) {
                    showNotification('Tasks refreshed successfully!', 'success');
                }
            } else {
                showNotification('Failed to load tasks: ' + response.message, 'error');
            }
        },
        error: function(xhr, status, error) {
            console.error('Error loading tasks:', error);
            showNotification('Error loading tasks. Please refresh the page.', 'error');
        },
        complete: function() {
            isLoading = false;
            hideLoadingSpinner();
        }
    });
}

// Add new task via AJAX
function addTaskAjax(description) {
    if (!description || description.trim() === '') {
        showNotification('Please enter a task description!', 'warning');
        return;
    }
    
    const taskData = {
        description: description.trim()
    };
    
    $.ajax({
        url: '/api/tasks',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(taskData),
        dataType: 'json',
        headers: {
            'X-CSRF-Token': getCsrfToken()
        },
        success: function(response) {
            if (response.success) {
                // Add new task to current tasks
                currentTasks.unshift(response.task);
                renderTasks(currentTasks);
                
                // Clear form
                $('#task-description-input').val('');
                
                showNotification(response.message, 'success');
                
                // Add success animation
                $('.task-item').first().addClass('task-added');
                setTimeout(() => {
                    $('.task-item').first().removeClass('task-added');
                }, 2000);
            } else {
                showNotification(response.message, 'error');
            }
        },
        error: function(xhr, status, error) {
            console.error('Error adding task:', error);
            showNotification('Error adding task. Please try again.', 'error');
        }
    });
}

// Update task via AJAX
function updateTaskAjax(taskId, description) {
    if (!description || description.trim() === '') {
        showNotification('Please enter a task description!', 'warning');
        return;
    }
    
    const taskData = {
        description: description.trim()
    };
    
    $.ajax({
        url: '/api/tasks/' + taskId,
        method: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(taskData),
        dataType: 'json',
        headers: {
            'X-CSRF-Token': getCsrfToken()
        },
        success: function(response) {
            if (response.success) {
                // Update task in current tasks
                const taskIndex = currentTasks.findIndex(task => task.id === taskId);
                if (taskIndex !== -1) {
                    currentTasks[taskIndex] = response.task;
                    renderTasks(currentTasks);
                }
                
                showNotification(response.message, 'success');
            } else {
                showNotification(response.message, 'error');
            }
        },
        error: function(xhr, status, error) {
            console.error('Error updating task:', error);
            showNotification('Error updating task. Please try again.', 'error');
        }
    });
}

// Delete task via AJAX
function deleteTaskAjax(taskId) {
    if (!confirm('Are you sure you want to delete this task?')) {
        return;
    }
    
    $.ajax({
        url: '/api/tasks/' + taskId,
        method: 'DELETE',
        dataType: 'json',
        headers: {
            'X-CSRF-Token': getCsrfToken()
        },
        success: function(response) {
            if (response.success) {
                // Remove task from current tasks
                currentTasks = currentTasks.filter(task => task.id !== taskId);
                renderTasks(currentTasks);
                
                showNotification(response.message, 'success');
            } else {
                showNotification(response.message, 'error');
            }
        },
        error: function(xhr, status, error) {
            console.error('Error deleting task:', error);
            showNotification('Error deleting task. Please try again.', 'error');
        }
    });
}


// ========================================
// MODERN UI RENDERING
// ========================================

// Render tasks with modern UI
function renderTasks(tasks) {
    const taskList = $('#modern-task-list');
    
    if (tasks.length === 0) {
        taskList.html(`
            <div class="empty-state">
                <div class="empty-icon">📝</div>
                <h3>No tasks yet!</h3>
                <p>Add your first task above to get started.</p>
            </div>
        `);
        return;
    }
    
    let html = '';
    tasks.forEach(task => {
        html += `
            <div class="task-item" data-task-id="${task.id}">
                <div class="task-main">
                    <div class="task-content">
                        <div class="task-description">${escapeHtml(task.description)}</div>
                    </div>
                    <div class="task-actions">
                        <button class="btn btn-edit" onclick="startEditTask(${task.id})" title="Edit task">
                            <i class="icon-edit">✏️</i>
                        </button>
                        <button class="btn btn-delete" onclick="deleteTaskAjax(${task.id})" title="Delete task">
                            <i class="icon-delete">🗑️</i>
                        </button>
                    </div>
                </div>
                <div class="task-edit-form" id="edit-form-${task.id}" style="display: none;">
                    <div class="edit-input-group">
                        <input type="text" id="edit-input-${task.id}" value="${escapeHtml(task.description)}" class="form-control">
                        <button class="btn btn-save" onclick="saveTaskEdit(${task.id})">
                            <i class="icon-save">💾</i> Save
                        </button>
                        <button class="btn btn-cancel" onclick="cancelTaskEdit(${task.id})">
                            <i class="icon-cancel">❌</i> Cancel
                        </button>
                    </div>
                </div>
            </div>
        `;
    });
    
    taskList.html(html);
}

// ========================================
// EVENT HANDLERS
// ========================================

// Setup AJAX form handlers
function setupAjaxHandlers() {
    // Add task form handler
    $('#add-task-form').on('submit', function(e) {
        e.preventDefault();
        const description = $('#task-description-input').val();
        addTaskAjax(description);
    });
    
    // Enter key handler for add task input
    $('#task-description-input').on('keypress', function(e) {
        if (e.which === 13) { // Enter key
            e.preventDefault();
            const description = $(this).val();
            addTaskAjax(description);
        }
    });
}

// Setup view toggle handlers
function setupViewToggle() {
    $('.toggle-btn').on('click', function() {
        const viewType = $(this).data('view');
        
        // Update active button
        $('.toggle-btn').removeClass('active');
        $(this).addClass('active');
        
        // Toggle view
        toggleView(viewType);
    });
}

// Toggle between list and grid view
function toggleView(viewType) {
    const taskList = $('.modern-task-list');
    
    if (viewType === 'grid') {
        taskList.removeClass('list-view').addClass('grid-view');
        console.log('📋 Switched to grid view');
    } else {
        taskList.removeClass('grid-view').addClass('list-view');
        console.log('📋 Switched to list view');
    }
}

// Start editing a task
function startEditTask(taskId) {
    const editForm = $(`#edit-form-${taskId}`);
    const taskItem = $(`[data-task-id="${taskId}"]`);
    
    taskItem.find('.task-main').hide();
    editForm.show();
    editForm.find('input').focus().select();
}

// Save task edit
function saveTaskEdit(taskId) {
    const newDescription = $(`#edit-input-${taskId}`).val();
    updateTaskAjax(taskId, newDescription);
}

// Cancel task edit
function cancelTaskEdit(taskId) {
    const editForm = $(`#edit-form-${taskId}`);
    const taskItem = $(`[data-task-id="${taskId}"]`);
    
    editForm.hide();
    taskItem.find('.task-main').show();
    
    // Reset input to original value
    const originalDescription = taskItem.find('.task-description').text();
    editForm.find('input').val(originalDescription);
}

// ========================================
// NOTIFICATION SYSTEM
// ========================================

// Show notification
function showNotification(message, type = 'info') {
    const notification = $(`
        <div class="notification notification-${type}">
            <div class="notification-content">
                <span class="notification-message">${message}</span>
                <button class="notification-close" onclick="closeNotification(this)">×</button>
            </div>
        </div>
    `);
    
    $('.notification-container').append(notification);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        notification.fadeOut(300, function() {
            $(this).remove();
        });
    }, 5000);
}

// Close notification
function closeNotification(element) {
    $(element).closest('.notification').fadeOut(300, function() {
        $(this).remove();
    });
}

// Setup notifications container
function setupNotifications() {
    if ($('.notification-container').length === 0) {
        $('body').append('<div class="notification-container"></div>');
    }
}

// ========================================
// LOADING STATES
// ========================================

// Show loading spinner
function showLoadingSpinner() {
    if ($('.loading-spinner').length === 0) {
        $('.task-list').append('<div class="loading-spinner"><div class="spinner"></div><span>Loading tasks...</span></div>');
    }
}

// Hide loading spinner
function hideLoadingSpinner() {
    $('.loading-spinner').remove();
}

// ========================================
// UTILITY FUNCTIONS
// ========================================

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, function(m) { return map[m]; });
}

// ========================================
// LEGACY COMPATIBILITY FUNCTIONS
// ========================================

function toggleEdit(taskId) {
    const editForm = document.getElementById('edit-form-' + taskId);
    const taskContent = editForm.previousElementSibling;
    
    if (editForm.style.display === 'none' || editForm.style.display === '') {
        // Show edit form
        editForm.style.display = 'block';
        taskContent.style.display = 'none';
        
        // Focus on input field
        const input = editForm.querySelector('input[type="text"]');
        input.focus();
        input.select();
    } else {
        // Hide edit form
        editForm.style.display = 'none';
        taskContent.style.display = 'flex';
    }
}

function cancelEdit(taskId) {
    const editForm = document.getElementById('edit-form-' + taskId);
    const taskContent = editForm.previousElementSibling;
    
    // Hide edit form
    editForm.style.display = 'none';
    taskContent.style.display = 'flex';
    
    // Reset form to original value
    const input = editForm.querySelector('input[type="text"]');
    const originalValue = input.getAttribute('data-original-value') || input.value;
    input.value = originalValue;
}

// ========================================
// ADMIN DASHBOARD JAVASCRIPT FUNCTIONS
// ========================================

function showUserDetails(userId) {
    // Get user data from the card
    const userCard = document.querySelector(`[onclick="showUserDetails(${userId})"]`);
    const email = userCard.querySelector('.user-email').textContent;
    const role = userCard.querySelector('.role-badge').textContent.toLowerCase();
    
    const modal = document.getElementById('userDetailModal');
    const detailsDiv = document.getElementById('userDetails');
    
    detailsDiv.innerHTML = 
        '<div class="user-details">' +
          '<div class="detail-row">' +
            '<span class="detail-label">ID:</span>' +
            '<span class="detail-value">' + userId + '</span>' +
          '</div>' +
          '<div class="detail-row">' +
            '<span class="detail-label">Email:</span>' +
            '<span class="detail-value">' + email + '</span>' +
          '</div>' +
          '<div class="detail-row">' +
            '<span class="detail-label">Role:</span>' +
            '<span class="detail-value">' +
              '<span class="role-badge ' + role + '">' + role.toUpperCase() + '</span>' +
            '</span>' +
          '</div>' +
        '</div>' +
        '<div class="form-actions">' +
          '<button onclick="toggleEdit(' + userId + ')" class="btn btn-outline-primary">Edit User</button>' +
          '<button onclick="deleteUser(' + userId + ')" class="btn btn-danger">Delete User</button>' +
        '</div>';
    
    modal.style.display = 'flex';
}

function closeModal() {
    document.getElementById('userDetailModal').style.display = 'none';
}

function toggleEdit(userId) {
    closeModal(); // Close detail modal first
    
    const editForm = document.getElementById('edit-form-' + userId);
    const editModal = document.getElementById('editModal');
    const editFormDiv = document.getElementById('editForm');
    
    editFormDiv.innerHTML = editForm.innerHTML;
    editModal.style.display = 'flex';
}

function closeEditModal() {
    document.getElementById('editModal').style.display = 'none';
}

function deleteUser(userId) {
    if (confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
        const deleteForm = document.getElementById('delete-form-' + userId);
        deleteForm.submit();
    }
}

// ========================================
// MODAL MANAGEMENT
// ========================================

// Close modals when clicking outside
window.onclick = function(event) {
    const userModal = document.getElementById('userDetailModal');
    const editModal = document.getElementById('editModal');
    
    if (event.target === userModal) {
        closeModal();
    }
    if (event.target === editModal) {
        closeEditModal();
    }
}

// ========================================
// RESPONSIVE BEHAVIOR
// ========================================

function handleResponsive() {
    const usersGrid = document.querySelector('.users-grid');
    const modalContent = document.querySelector('.modal-content');
    
    if (window.innerWidth <= 768) {
        if (usersGrid) {
            usersGrid.style.gridTemplateColumns = '1fr';
        }
        if (modalContent) {
            modalContent.style.width = '95%';
            modalContent.style.margin = '10px';
        }
    } else {
        if (usersGrid) {
            usersGrid.style.gridTemplateColumns = 'repeat(auto-fill, minmax(300px, 1fr))';
        }
        if (modalContent) {
            modalContent.style.width = '90%';
            modalContent.style.margin = 'auto';
        }
    }
}

// ========================================
// INITIALIZATION
// ========================================

// Store original values when page loads
document.addEventListener('DOMContentLoaded', function() {
    const editInputs = document.querySelectorAll('.task-edit-form input[type="text"]');
    editInputs.forEach(input => {
        input.setAttribute('data-original-value', input.value);
    });
});

// Run responsive behavior on load and resize
window.addEventListener('load', handleResponsive);
window.addEventListener('resize', handleResponsive);
