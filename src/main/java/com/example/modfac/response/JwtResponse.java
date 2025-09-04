package com.example.modfac.response;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String username;
    private String role;

    private JwtResponse(Builder builder) {
        this.token = builder.token;
        this.username = builder.username;
        this.role = builder.role;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private String username;
        private String role;

        public Builder() {}

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public JwtResponse build() {
            return new JwtResponse(this);
        }
    }
}
