// Task List JavaScript Functions

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

// Store original values when page loads
document.addEventListener('DOMContentLoaded', function() {
    const editInputs = document.querySelectorAll('.task-edit-form input[type="text"]');
    editInputs.forEach(input => {
        input.setAttribute('data-original-value', input.value);
    });
});
