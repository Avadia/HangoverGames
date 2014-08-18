package net.zyuiop.HangoverGames.Network;


public enum Status {
	
        Available("available", 80),
        Idle("idle", 70),
        Starting("starting", 30),
        Stopping("stopping", 20),
        InGame("ingame", 10);

        private String info;
        private int value;

        private Status(String info, int value) {
            this.info = info;
            this.value = value;
        }

        public String getString() {
            return info;
        }

        public int getValue() {
            return value;
        }

        public boolean isLobby() {
            if (value == Status.Available.getValue()
                    || value == Status.Starting.getValue()) {
                return true;
            }

            return false;
        }

        public boolean isIG() {
            if (value == Status.InGame.getValue()
                    || value == Status.Stopping.getValue()) {
                return true;
            }
            return false;
        }

    
}
