<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>TrackMe - ${pageName}</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.2/css/bootstrap.min.css" integrity="sha384-PsH8R72JQ3SOdhVi3uxftmaW6Vc51MKb0q5P2rRUpPvrszuE4W1povHYgTpBfshb" crossorigin="anonymous">
    <script>
    	function submitForReset() {
    		var password = document.getElementById("password").value;
    		var confirmationPassword = document.getElementById("confirmPassword").value;
    		if(password == null || password == "" || confirmationPassword == null || confirmationPassword == ""){
    			alert("Please enter both password and confirmation password");
    			clearFields();
    		} else if(password.length < 8 || password.length > 20) {
    			alert("Password should be at least 8 characters and less than 20 characters in length");
    			clearFields();
    		} else if(password != confirmationPassword) {
    			alert("Password and confirmation password doest not match");
    			clearFields();
    		} else {
    			document.resetPasswordForm.submit();
    		}
    	}
    	
    	function clearFields() {
    		document.getElementById("password").value = "";
    		document.getElementById("confirmPassword").value = "";
    	}
    </script>
</head>
<body>
    <div class="container">
        <h2 class="text-center mt-5">TrackMe User - ${pageName}</h2>
        <div class="row">
            <div class="col-4 mt-5" style="margin: 0 auto">
                <form action="changePassword" method="POST" name="resetPasswordForm" id="resetPasswordForm">
                    <c:choose>
	                  <c:when test="${empty error}">
							<input type="hidden" id="temporaryToken" name="temporaryToken" value="${token}"/>
							<div class="form-group">
	                        	<label for="password">Password</label>
	                        	<input type="password" class="form-control" name="password" id="password" placeholder="Enter Password">
	                    	</div>
		                    <div class="form-group">
		                        <label for="confirm_password">Confirm Password</label>
		                        <input type="password" class="form-control" name="confirmPassword" id="confirmPassword" placeholder="Confirm Password">
		                    </div>
	                    	<button type="button" class="btn btn-primary" onclick="submitForReset()">Reset Password</button>
							<input type="hidden" id="hiddenJSON" name="hiddenJSON"/>
	                  </c:when>
	                  <c:otherwise>
							<div class="errorBlock">
		                  		<p>${error.error}</p>
		                  	</div>
	                  </c:otherwise>
            		</c:choose>
            		<p>${successMessage}</p>
                </form>
              
            </div>
        </div>
    </div>
</body>
</html>