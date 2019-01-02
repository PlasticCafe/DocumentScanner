package cafe.plastic.documentscanner.ui.fragments;

public class CameraState {
        public Flash flash;
        public Focus focus;
        public Outline outline;

        public CameraState(Flash flash, Focus focus, Outline outline) {
            this.flash = flash;
            this.focus = focus;
            this.outline = outline;
        }

        public enum Flash {
            OFF,
            ON,
            AUTO
        }

        public enum Focus {
            AUTO,
            FIXED
        }

        public enum Outline {
            ON,
            OFF
        }
    }
