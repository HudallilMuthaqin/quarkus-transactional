package org.testing.transactional.common;

import java.net.URI;

public final class ErrorConstants {
    // ************************************************************
    // Framework Special Constant
    // ************************************************************
    public static final String CONTACT_THE_ADMINISTRATOR = "Something went wrong, please contact the administrator";

    public static final String DATA_EXIST = "Data already existed";
    public static final String DATA_NOT_FOUND = "Data not found";
    public static final String DATA_NOT_UNIQUE = "Data is already existed. Please try another one.";
    public static final String DATA_CACHE_FAILED_TO_GENERATE = "Data cache failed to generate";
    public static final String EMPTY_FIELD = "Please, fill in the mandatory field.";

    public static final Integer ERROR_HANDLING_100 = 100;
    public static final Integer ERROR_HANDLING_400 = 400;
    public static final Integer ERROR_HANDLING_401 = 401;
    public static final Integer ERROR_HANDLING_403 = 403;
    public static final Integer ERROR_HANDLING_404 = 404;
    public static final Integer ERROR_HANDLING_422 = 422;
    public static final Integer ERROR_HANDLING_500 = 500;
//    public static final String NOT_AUTHORIZED = "NOT_AUTHORIZED";
//    public static final String AUTHORIZED = "AUTHORIZED";

    public static final String NEW_USER_ID_MUST_BE_EMPTY = "A new user cannot already have an ID";
    public static final String LOGIN_NAME_ALREADY_USED = "Login name already used.";
    public static final String EMAIL_ALREADY_USED = "Email is already in use.";
    public static final String NOT_AUTHORIZED = "User does not eligible to authorize.";
    public static final String AUTHORIZED = "Data has been authorized.";
    public static final String USER_NOT_FOUND = "User could not be found.";
    public static final String CURRENT_USER_LOGIN_NOT_FOUND = "Current user login could not be found.";
    public static final String NO_USER_FOUND_FOR_ACTIVATION = "No user was found for this activation key.";
    public static final String EMAIL_ADDRESS_NOT_REGISTERED = "Email address not registered.";

    public static final String USER_EMAIL_NOT_UNIQUE = "User email is not unique";
    public static final String USER_EMAIL_NOT_EMPTY = "User email must be not empty";
    public static final String USER_ID_ANONYMOUS = "Username is not found";
    public static final String USER_LOGIN_NOT_UNIQUE = "User login is not unique";
    public static final String USER_SHOULD_LOGIN = "Please login first";

    public static final String BRANCH_CODE_NOT_UNIQUE = "Branch code is not unique";
    public static final String BRANCH_NOT_FOUND = "Branch Not Found";
    public static final String BRANCH_NOT_EMPTY = "Branch id must be not empty";

    public static final String REGION_CODE_NOT_UNIQUE = "Region code is not unique";
    public static final String REGION_NOT_FOUND = "Region Not Found";

    public static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String PROBLEM_BASE_URL = "https://epurseimp.bni.co.id/problem"; // change into name of application
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
    public static final URI INVALID_PASSWORD_TYPE = URI.create(PROBLEM_BASE_URL + "/invalid-password");
    public static final URI EMAIL_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/email-already-used");
    public static final URI LOGIN_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/login-already-used");
    public static final URI EMAIL_NOT_FOUND_TYPE = URI.create(PROBLEM_BASE_URL + "/email-not-found");

    public static final String CARD_NUMBER_ALREAD_EXIST = "Card Number Already Exist";
    public static final String CARD_NUMBER_DUPLICATE = "Card Number Duplicate : ";
    private ErrorConstants() {
    }
}