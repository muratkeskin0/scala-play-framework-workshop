// ========================================
// TASK LIST JAVASCRIPT FUNCTIONS
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
