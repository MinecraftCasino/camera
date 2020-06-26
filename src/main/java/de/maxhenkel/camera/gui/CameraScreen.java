package de.maxhenkel.camera.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.camera.*;
import de.maxhenkel.camera.net.MessageRequestUploadCustomImage;
import de.maxhenkel.camera.net.MessageSetShader;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

public class CameraScreen extends ScreenBase {

    private static final ResourceLocation CAMERA_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/camera.png");
    private static final int FONT_COLOR = 4210752;

    private int index = 0;

    public CameraScreen(String currentShader) {
        super(CAMERA_TEXTURE, new DummyContainer(), null, new TranslationTextComponent("gui.camera.title"));
        xSize = 248;
        ySize = 109;

        for (int i = 0; i < Shaders.SHADER_LIST.size(); i++) {
            String s = Shaders.SHADER_LIST.get(i);
            if (currentShader == null) {
                if (s.equals("none")) {
                    index = i;
                    break;
                }
            } else if (s.equals(currentShader)) {
                index = i;
                break;
            }
        }
    }

    @Override
    protected void func_231160_c_() {
        super.func_231160_c_();
        field_230710_m_.clear();
        int padding = 10;
        int buttonWidth = 70;
        int buttonHeight = 20;
        Button prev = func_230480_a_(new Button(guiLeft + padding, guiTop + ySize / 2 - buttonHeight / 2, buttonWidth, buttonHeight, new TranslationTextComponent("button.camera.prev"), button -> {
            index--;
            if (index < 0) {
                index = Shaders.SHADER_LIST.size() - 1;
            }
            sendShader();
        }));
        prev.field_230693_o_ = false; //TODO fix shaders
        Button next = func_230480_a_(new Button(guiLeft + xSize - buttonWidth - padding, guiTop + ySize / 2 - buttonHeight / 2, buttonWidth, buttonHeight, new TranslationTextComponent("button.camera.next"), button -> {
            index++;
            if (index >= Shaders.SHADER_LIST.size()) {
                index = 0;
            }
            sendShader();
        }));
        next.field_230693_o_ = false; //TODO fix shaders

        if (Config.SERVER.ALLOW_IMAGE_UPLOAD.get()) {
            func_230480_a_(new Button(guiLeft + xSize / 2 - buttonWidth / 2, field_230709_l_ / 2 + ySize / 2 - buttonHeight - padding, buttonWidth, buttonHeight, new TranslationTextComponent("button.camera.upload"), button -> {
                ImageTools.chooseImage(file -> {
                    try {
                        UUID uuid = UUID.randomUUID();
                        BufferedImage image = ImageTools.loadImage(file);
                        ClientImageUploadManager.addImage(uuid, image);
                        Main.SIMPLE_CHANNEL.sendToServer(new MessageRequestUploadCustomImage(uuid));
                    } catch (IOException e) {
                        playerInventory.player.sendMessage(new TranslationTextComponent("message.upload_error", e.getMessage()), playerInventory.player.getUniqueID());
                        e.printStackTrace();
                    }
                    field_230706_i_.currentScreen = null;
                });
            }));
        }
    }

    private void sendShader() {
        Main.SIMPLE_CHANNEL.sendToServer(new MessageSetShader(Shaders.SHADER_LIST.get(index)));
    }

    @Override
    protected void func_230451_b_(MatrixStack matrixStack, int x, int y) {
        super.func_230451_b_(matrixStack, x, y);

        String title = new TranslationTextComponent("gui.camera.choosefilter").getString();

        int titleWidth = field_230712_o_.getStringWidth(title);

        field_230712_o_.func_238421_b_(matrixStack, title, xSize / 2 - titleWidth / 2, 10, FONT_COLOR);

        String shaderName = new TranslationTextComponent("shader." + Shaders.SHADER_LIST.get(index)).getString();

        int shaderWidth = field_230712_o_.getStringWidth(shaderName);

        field_230712_o_.func_238421_b_(matrixStack, shaderName, xSize / 2 - shaderWidth / 2, ySize / 2 - field_230712_o_.FONT_HEIGHT / 2, 0xFFFFFFFF);
    }
}