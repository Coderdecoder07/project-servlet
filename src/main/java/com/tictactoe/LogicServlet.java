package com.tictactoe;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;


@WebServlet(name = "LogicServlet", value = "/logic")
public class LogicServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Get the current session
        HttpSession currentSession = req.getSession();

        // Get the field object from the session
        Field field = extractField(currentSession);

        // Get the index of the cell clicked by the user
        int index = getSelectedIndex(req);
        Sign currentSign = field.getField().get(index);

        if (Sign.EMPTY != currentSign) {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
            dispatcher.forward(req, resp);
            return;
        }

        // Update the field with a cross at the selected index
        field.getField().put(index, Sign.CROSS);

        // Check for an empty cell to place a nought
        int emptyFieldIndex = field.getEmptyFieldIndex();

        if (emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
        }
        else {
            // If no empty cell is found, it's a draw
            currentSession.setAttribute("draw", true);
            List<Sign> data = field.getFieldData();

            // Update the session attributes with the new field data
            currentSession.setAttribute("data", data);
        }
        // Get the updated list of field values
        List<Sign> data = field.getFieldData();

        // Update the session attributes with the new field data
        currentSession.setAttribute("data", data);
        currentSession.setAttribute("field", field);

        resp.sendRedirect("/index.jsp");

        if (checkWin(resp, currentSession, field)) {
            return;
        }
        if (emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
            // Check for a win after placing the nought
            if (checkWin(resp, currentSession, field)) {
                return;
            }
        }
    }

    private int getSelectedIndex(HttpServletRequest request) {
        String click = request.getParameter("click");
        boolean isNumeric = click.chars().allMatch(Character::isDigit);
        return isNumeric ? Integer.parseInt(click) : 0;
    }

    private Field extractField(HttpSession currentSession) {
        Object fieldAttribute = currentSession.getAttribute("field");
        if (Field.class != fieldAttribute.getClass()) {
            currentSession.invalidate();
            throw new RuntimeException("Session is broken, try one more time");
        }
        return (Field) fieldAttribute;
    }

    private boolean checkWin(HttpServletResponse response, HttpSession currentSession, Field field) throws IOException {
        Sign winner = field.checkWin();
        if (Sign.CROSS == winner || Sign.NOUGHT == winner) {
            currentSession.setAttribute("winner", winner);
            List<Sign> data = field.getFieldData();
            currentSession.setAttribute("data", data);
            return true;
        }
        return false;
    }
}