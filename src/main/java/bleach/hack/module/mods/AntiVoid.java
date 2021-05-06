/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventClientMove;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.FabricReflect;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class AntiVoid extends Module {

	public AntiVoid() {
		super("AntiVoid", KEY_UNBOUND, Category.MOVEMENT, "Prevents you from falling in the void",
				new SettingMode("Mode", "Jump", "Floor", "Vanilla").withDesc("What mode to use when you're in the void"),
				new SettingToggle("AntiTP", true).withDesc("Prevents you from accidentally tping in to the void (i.e., using PacketFly)"));
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (mc.player.getY() < 0) {
			switch (getSetting(0).asMode().mode) {
				case 0:
					mc.player.jump();
					break;
				case 1:
					mc.player.setOnGround(true);
					break;
				case 2:
					for (int i = 3; i < 257; i++) {
						if (WorldUtils.isBoxEmpty(mc.player.getBoundingBox().offset(0, -mc.player.getY() + i, 0))) {
							mc.player.updatePosition(mc.player.getX(), i, mc.player.getZ());
							break;
						}
					}

					break;
			}
		}
	}

	@Subscribe
	public void onSendPacket(EventSendPacket event) {
		if (event.getPacket() instanceof PlayerMoveC2SPacket) {
			PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket) event.getPacket();

			if (getSetting(1).asToggle().state
					&& mc.player.getY() >= 0 && packet.getY(mc.player.getY()) < 0) {
				event.setCancelled(true);
				return;
			}
			
			if (getSetting(0).asMode().mode == 1 && mc.player.getY() < 0 && packet.getY(mc.player.getY()) < mc.player.getY()) {
				FabricReflect.writeField(packet, mc.player.getY(), "field_12886", "y");
			}
		}
	}

	@Subscribe
	public void onClientMove(EventClientMove event) {
		if (getSetting(1).asToggle().state && mc.player.getY() >= 0 && mc.player.getY() - event.vec3d.y < 0) {
			event.setCancelled(true);
			return;
		}
		
		if (getSetting(0).asMode().mode == 1 && mc.player.getY() < 0 && event.vec3d.y < 0) {
			event.vec3d = new Vec3d(event.vec3d.x, 0, event.vec3d.z);
			mc.player.addVelocity(0, -mc.player.getVelocity().y, 0);
		}
	}

}