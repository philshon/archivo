/*
 * Copyright 2015-2016 Todd Kulesza <todd@dropline.net>.
 *
 * This file is part of Archivo.
 *
 * Archivo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archivo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archivo.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.straylightlabs.archivo.model;

import net.straylightlabs.archivo.utilities.OSHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum FileType {
    ANDROID_PHONE("Android Files", "*.mp4"),
    ANDROID_TABLET("Android Tablet Files", "*.mp4"),
    //    APPLE_TV("AppleTV 1 & 2 Files", "*.m4v"),
    APPLE_TV3("AppleTV 3 Files", "*.m4v"),
    TS("Decrypted TiVo Files", "*.ts", false, false, false),
    TIVO("Encrypted TiVo Files", "*.TiVo", false, false, false),
    IPAD("iPad Files", "*.mp4"),
    IPHONE("iPhone & iPod Touch Files", "*.mp4"),
    PYTIVO("PyTivo Files", "*.ts", false, true, false),
    H264_NORMAL("Standard H.264 Files", "*.mp4"),
    H264_HIGH("Standard H.264 (High Profile) Files ", "*.mp4"),
    H265("Standard H.265 Files", "*.mp4", true, true, false),
    WINDOWS_PHONE("Windows Phone Files", "*.mp4");

    private final String description;
    private final String extension;
    private final boolean needsTranscoding;
    private final boolean includeMetadata;
    private final boolean supportsQSV;

    private static final Map<FileType, Map<String, String>> handbrakeArgs;

    static {
        handbrakeArgs = buildHandbrakeArgsMap();
    }

    FileType(String description, String extension) {
        this(description, extension, true, false, true);
    }

    FileType(String description, String extension, boolean needsTranscoding, boolean includeMetadata,
             boolean supportsQSV) {
        this.description = description;
        this.extension = extension;
        this.needsTranscoding = needsTranscoding;
        this.includeMetadata = includeMetadata;
        this.supportsQSV = supportsQSV;
    }

    public static FileType fromDescription(String description) {
        for (FileType ft : values()) {
            if (ft.description.equalsIgnoreCase(description)) {
                return ft;
            }
        }
        throw new IllegalArgumentException("Unknown description: " + description);
    }

    public static FileType getDefault() {
        return H264_NORMAL;
    }

    public boolean supportsQSV() {
        return supportsQSV;
    }
    public String getDescription() {
        return description;
    }

    public String getExtension() {
        return extension;
    }

    public boolean needsTranscoding() {
        return needsTranscoding;
    }

    public boolean includeMetadata() {
        return includeMetadata;
    }

    public Map<String, String> getHandbrakeArgs() {
        return Collections.unmodifiableMap(handbrakeArgs.getOrDefault(this, Collections.emptyMap()));
    }

    private static Map<FileType, Map<String, String>> buildHandbrakeArgsMap() {
        Map<FileType, Map<String, String>> map = new HashMap<>();

        // Android
        String args = String.format("-e x264 -q 22.0 -r 30 --pfr -a 1 -E %s -B 128 -6 dpl2 -R Auto -D 0.0 " +
                        "--audio-copy-mask aac,ac3,dtshd,dts,mp3 --audio-fallback ffac3 -f mp4 -X 720 -Y 576 " +
                        "--decomb=fast --normalize-mix 1 " +
                        "--loose-anamorphic --modulus 2 --x264-preset veryfast --h264-profile main --h264-level 3.0",
                getPlatformAudioEncoder());
        map.put(ANDROID_PHONE, parseArgs(args));

        // Android tablet
        args = String.format("-e x264 -q 22.0 -r 30 --pfr -a 1 -E %s -B 128 -6 dpl2 -R Auto -D 0.0 " +
                        "--audio-copy-mask aac,ac3,dtshd,dts,mp3 --audio-fallback ffac3 -f mp4 -X 1280 -Y 720 " +
                        "--decomb=fast --normalize-mix 1 " +
                        "--loose-anamorphic --modulus 2 --x264-preset veryfast --h264-profile main --h264-level 3.1",
                getPlatformAudioEncoder());
        map.put(ANDROID_TABLET, parseArgs(args));

        // AppleTV 3
        args = String.format("-e x264 -q 21.0 -r 30 --pfr -X 1920 -Y 1080 --audio-copy-mask aac,ac3,dtshd,dts,mp3 -a 1,1 " +
                        "-E %s,copy:ac3 -B 160,none -6 dpl2,none -R Auto,Auto -D 0.0,0.0 --audio-fallback  ffac3 -f m4v -4 " +
                        "--decomb=fast --normalize-mix 1 " +
                        "--loose-anamorphic --modulus 2 -m --x264-preset veryfast --h264-profile high --h264-level 4.0 -v",
                getPlatformAudioEncoder());
        map.put(APPLE_TV3, parseArgs(args));

        // iPad
        args = String.format("-e x264 -q 22.0 -r 30 --pfr -a 1 -E %s -B 160 -6 dpl2 -R Auto -D 0.0 " +
                        "--audio-copy-mask aac,ac3,dtshd,dts,mp3 --audio-fallback ffac3 -f mp4 -4 -X 1280 -Y 720 " +
                        "--decomb=fast --normalize-mix 1 " +
                        "--loose-anamorphic --modulus 2 -m --x264-preset veryfast --h264-profile high --h264-level 3.1",
                getPlatformAudioEncoder());
        map.put(IPAD, parseArgs(args));

        // iPhone & iPod Touch
        args = String.format("-e x264 -q 22.0 -r 30 --pfr -a 1 -E %s -B 160 -6 dpl2 -R Auto -D 0.0 " + "" +
                        "--audio-copy-mask aac,ac3,dtshd,dts,mp3 --audio-fallback ffac3 -f mp4 -4 -X 960 -Y 640 " +
                        "--decomb=fast --normalize-mix 1 " +
                        "--loose-anamorphic --modulus 2 -m --x264-preset veryfast --h264-profile high --h264-level 3.1",
                getPlatformAudioEncoder());
        map.put(IPHONE, parseArgs(args));

        // Windows Phone 8
        args = String.format("-e x264 -q 22.0 -a 1 -E %s -B 128 -6 dpl2 -R Auto -D 0.0 --audio-copy-mask aac,ac3,dtshd,dts,mp3 " +
                "--audio-fallback ffac3 -f mp4 --loose-anamorphic --modulus 2 -m --x264-preset veryfast --h264-profile main " +
                "--decomb=fast --normalize-mix 1 " +
                "--h264-level 3.1 -X 1280 -Y 720", getPlatformAudioEncoder());
        map.put(WINDOWS_PHONE, parseArgs(args));

        // Normal H264
        args = String.format("-e x264 -q 21.0 -a 1 -E %s -B 160 -6 dpl2 -R Auto -D 0.0 --audio-copy-mask aac,ac3,dtshd,dts,mp3 " +
                "--audio-fallback ffac3 -f mp4 --loose-anamorphic --modulus 2 -m --x264-preset veryfast --h264-profile main " +
                "--decomb=fast --normalize-mix 1 " +
                "--h264-level 4.0", getPlatformAudioEncoder());
        map.put(H264_NORMAL, parseArgs(args));

        // High Profile H264
        args = String.format("-e x264 -q 21.0 -a 1,1 -E %s,copy:ac3 -B 160,160 -6 dpl2,none -R Auto,Auto -D 0.0,0.0 " +
                "--audio-copy-mask aac,ac3,dtshd,dts,mp3 --audio-fallback ffac3 -f mp4 -4 -5 --loose-anamorphic " +
                "--normalize-mix 1 " +
                "--modulus 2 -m --x264-preset veryfast --h264-profile high --h264-level 4.1", getPlatformAudioEncoder());
        map.put(H264_HIGH, parseArgs(args));

        // H265
        args = String.format("-e x265 -q 21.0 -a 1,1 -E %s,copy:ac3 -B 160,160 -6 dpl2,none -R Auto,Auto -D 0.0,0.0 " +
                "--audio-copy-mask aac,ac3,dtshd,dts,mp3 --audio-fallback ffac3 -f mp4 -4 -5 --loose-anamorphic " +
                "--normalize-mix 1 " +
                "--modulus 2 -m --x265-preset veryfast ", getPlatformAudioEncoder());
        map.put(H265, parseArgs(args));

        return map;
    }

    private static Map<String, String> parseArgs(String argString) {
        Map<String, String> map = new HashMap<>();

        String[] args = argString.split("\\s+");
        int i;
        for (i = 0; i + 1 < args.length; i++) {
            if (args[i + 1].startsWith("-")) {
                // Single parameter
                map.put(args[i], null);
            } else {
                // Parameter with a value
                map.put(args[i], args[i + 1]);
                i++;
            }
        }
        if (i < args.length) {
            map.put(args[i], null);
        }

        return map;
    }

    public static String getPlatformAudioEncoder() {
        if (OSHelper.isMacOS()) {
            return "ca_aac";
        } else {
            return "aac";
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getDescription(), getExtension());
    }
}
