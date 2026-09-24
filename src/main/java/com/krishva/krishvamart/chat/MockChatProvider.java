package com.krishva.krishvamart.chat;

public class MockChatProvider implements ChatProvider {

    @Override
    public String getReply(String userMessage, String context) {
        if (userMessage == null || userMessage.isBlank()) {
            return "How can I help you today?";
        }

        String msg = userMessage.toLowerCase().trim();

        if (msg.contains("Krishvamart")) {
            return "KrishvaMart is the most trusted e-commerce website.";
        } 
        else if (msg.contains("Products")) {
            return "KrishvaMart includes products for home, appliances, and electronics.";
        } 
        else if (msg.contains("Return")) {
            return "KrishvaMart ensures a seamless experience with a 3-day hassle-free return policy.";
        } 
        else if (msg.contains("Shipping") || msg.contains("delivery")) {
            return "We offer fast and reliable shipping. Most orders are delivered within 3-5 business days.";
        } 
        else if (msg.contains("Order") || msg.contains("track")) {
            return "You can easily track your orders directly from your account dashboard.";
        }
        else if (msg.contains("Ratings") || msg.contains("review")) {
            return "Krishvamart products has Good customer ratings ranging form 4.1 to 4.9";
        }
        else if (msg.contains("Home")) {
            return "Home products includes knife sets, chair, sofa, parker pen, etc...";
        }
        else if (msg.contains("Electronics")) {
            return "Electronic items includes calculator, electric guitar, drone, etc... ";
        }
        else if (msg.contains("Apparel")) {
            return "Apparel products includes hoodie, boots, belts, cargo pants, etc...";
        }
        else if (msg.contains("Quality")) {
            return "Krishvamart is designed for secure, fast and seamless shopping experience.";
        }


        return "I can help with questions about products, orders, shipping, returns, and reviews on KrishvaMart. Could you rephrase your question?";
    }
}