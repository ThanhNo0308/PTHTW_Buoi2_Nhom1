import cookie from "react-cookies";

const MyUserReducer = (currentState, action) => {
    switch (action.type) {
        case "login":
            return action.payload;
        case "logout":
            cookie.remove("token");
            cookie.remove("user");
            return null;
        case "update":
            const updatedUser = {...currentState, ...action.payload};
            cookie.remove("user");
            cookie.save("user", updatedUser, { path: "/" });
            console.log("User context updated:", updatedUser);
            return updatedUser;
    }

    return currentState;
}

export default MyUserReducer;