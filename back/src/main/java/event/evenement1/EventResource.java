package event.evenement1;

import event.evenement1.bd.DatabaseConnection;
import event.evenement1.model.Event;
import event.evenement1.model.Category;
import event.evenement1.model.Users;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Path("/events")
public class EventResource {

   
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createEvent(Event event) {
        String query = "INSERT INTO events ( name, description, date, location, type, joinCode, limitParticipants, category_id, organizer_id) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, event.getName());
            ps.setString(2, event.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(event.getDate()));
            ps.setString(4, event.getLocation());
            ps.setString(5, event.getType());
            ps.setString(6, event.getJoinCode());
            ps.setInt(7, event.getLimitParticipants());
            ps.setLong(8, event.getCategory().getId());
            ps.setLong(9, event.getOrganizer().getId());

            ps.executeUpdate();
            return Response.status(Response.Status.CREATED)
                           .entity("Event created successfully!")
                           .build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error creating event: " + e.getMessage())
                           .build();
        }
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllEvents() {
        String query = "SELECT * FROM events";
        List<Event> events = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }
            return Response.ok(events).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error retrieving events: " + e.getMessage())
                           .build();
        }
    }


    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventById(@PathParam("id") String id) {
        String query = "SELECT * FROM events WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Response.ok(mapResultSetToEvent(rs)).build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND)
                                   .entity("Event not found with ID: " + id)
                                   .build();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error retrieving event: " + e.getMessage())
                           .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateEvent(@PathParam("id") String id, Event event) {
        String query = "UPDATE events SET name = ?, description = ?, date = ?, location = ?, type = ?, joinCode = ?, limitParticipants = ?, category_id = ?, organizer_id = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, event.getName());
            ps.setString(2, event.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(event.getDate()));
            ps.setString(4, event.getLocation());
            ps.setString(5, event.getType());
            ps.setString(6, event.getJoinCode());
            ps.setInt(7, event.getLimitParticipants());
            ps.setLong(8, event.getCategory().getId());
            ps.setLong(9, event.getOrganizer().getId());
            ps.setString(10, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return Response.ok("Event updated successfully!").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("Event not found with ID: " + id)
                               .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error updating event: " + e.getMessage())
                           .build();
        }
    }

  
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteEvent(@PathParam("id") String id) {
        String query = "DELETE FROM events WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                return Response.ok("Event deleted successfully!").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("Event not found with ID: " + id)
                               .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error deleting event: " + e.getMessage())
                           .build();
        }
    }
//filtrage par nom de categorie:
    @GET
    @Path("/category/name/{categoryName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventsByCategoryName(@PathParam("categoryName") String categoryName) {
        String query = "SELECT e.* FROM events e " +
                       "JOIN categories c ON e.category_id = c.id " +
                       "WHERE c.name = ?";

        List<Event> events = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, categoryName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(mapResultSetToEvent(rs));
                }
            }

            if (events.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("No events found for category name: " + categoryName)
                               .build();
            }
            return Response.ok(events).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error retrieving events by category name: " + e.getMessage())
                           .build();
        }
    }
//recherche par nom d'evenement
    @GET
    @Path("/search/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchEventByName(@PathParam("name") String name) {
        String query = "SELECT * FROM events WHERE name LIKE ?";
        List<Event> events = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, "%" + name + "%"); // Recherche partielle
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(mapResultSetToEvent(rs));
                }
            }

            if (events.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("No events found with name containing: " + name)
                               .build();
            }

            return Response.ok(events).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error searching event by name: " + e.getMessage())
                           .build();
        }
    }

    
    private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
        return new Event(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getTimestamp("date").toLocalDateTime(),
            rs.getString("location"),
            rs.getString("type"),
            rs.getString("joinCode"),
            rs.getInt("limitParticipants"),
            new Category(rs.getLong("category_id"), null), 
            new Users(rs.getLong("organizer_id"), null, null, null) 
        );
    }

    @POST
    @Path("/{eventId}/participate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response participateInEvent(@PathParam("eventId") Long eventId, Users user) {
        String checkEventQuery = "SELECT limitParticipants FROM events WHERE id = ?";
        String checkParticipantQuery = "SELECT * FROM event_participants WHERE event_id = ? AND user_id = ?";
        String addParticipantQuery = "INSERT INTO event_participants (event_id, user_id) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {
            // Vérifier si l'événement existe et s'il reste des places
            try (PreparedStatement psCheckEvent = connection.prepareStatement(checkEventQuery)) {
                psCheckEvent.setLong(1, eventId);
                try (ResultSet rs = psCheckEvent.executeQuery()) {
                    if (!rs.next()) {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Event not found with ID: " + eventId)
                                .build();
                    }
                    int limitParticipants = rs.getInt("limitParticipants");


                    String countParticipantsQuery = "SELECT COUNT(*) AS participant_count FROM event_participants WHERE event_id = ?";
                    try (PreparedStatement psCount = connection.prepareStatement(countParticipantsQuery)) {
                        psCount.setLong(1, eventId);
                        try (ResultSet rsCount = psCount.executeQuery()) {
                            if (rsCount.next() && rsCount.getInt("participant_count") >= limitParticipants) {
                                return Response.status(Response.Status.FORBIDDEN)
                                        .entity("Event is full. No more participants can join.")
                                        .build();
                            }
                        }
                    }
                }
            }

            try (PreparedStatement psCheckParticipant = connection.prepareStatement(checkParticipantQuery)) {
                psCheckParticipant.setLong(1, eventId);
                psCheckParticipant.setLong(2, user.getId());
                try (ResultSet rs = psCheckParticipant.executeQuery()) {
                    if (rs.next()) {
                        return Response.status(Response.Status.CONFLICT)
                                .entity("User already participating in this event.")
                                .build();
                    }
                }
            }

            try (PreparedStatement psAddParticipant = connection.prepareStatement(addParticipantQuery)) {
                psAddParticipant.setLong(1, eventId);
                psAddParticipant.setLong(2, user.getId());
                psAddParticipant.executeUpdate();
                return Response.status(Response.Status.CREATED)
                        .entity("User successfully added to the event!")
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error participating in event: " + e.getMessage())
                    .build();
        }
    }
    //leave
    @DELETE
    @Path("/{eventId}/leave")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response leaveEvent(@PathParam("eventId") Long eventId, Users user) {
        String queryCheck = "SELECT * FROM event_participants WHERE event_id = ? AND user_id = ?";
        String queryDelete = "DELETE FROM event_participants WHERE event_id = ? AND user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement psCheck = connection.prepareStatement(queryCheck);
             PreparedStatement psDelete = connection.prepareStatement(queryDelete)) {
            psCheck.setLong(1, eventId);
            psCheck.setLong(2, user.getId());
            try (ResultSet rs = psCheck.executeQuery()) {
                if (!rs.next()) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("User is not participating in this event.")
                            .build();
                }
            }

            psDelete.setLong(1, eventId);
            psDelete.setLong(2, user.getId());
            int rowsAffected = psDelete.executeUpdate();

            if (rowsAffected > 0) {
                return Response.status(Response.Status.OK)
                        .entity("User successfully removed from the event.")
                        .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to remove user from the event.")
                        .build();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error leaving event: " + e.getMessage())
                    .build();
        }
    }
}