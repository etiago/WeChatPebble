#include <pebble.h>

Window *window;
//TextLayer hello_layer;

static BitmapLayer *character_layer;
static GBitmap character_bitmap;
// 64 is number of bytes (uint8_t)
// each chinese character = 32 bytes (2 wide by 16)
// each western character = 16 bytes (1 wide by 16)
// 288 bytes is 144 pixels across, divided by 8 to get bytes times 16 rows
#define BITMAP_WIDTH 20
#define BITMAP_HEIGHT 160
static uint8_t character_data[BITMAP_WIDTH * BITMAP_HEIGHT];

static int offset = 0;


void in_received(DictionaryIterator *received, void *context) {
        app_comm_set_sniff_interval(SNIFF_INTERVAL_REDUCED);
        app_comm_set_sniff_interval(SNIFF_INTERVAL_NORMAL);
        
        Tuple* message = dict_find(received, 1);
        
        Tuple* reset = dict_find(received, 2);
        
        Tuple* finished = dict_find(received, 3);
        
        if (reset != NULL) {
                offset = 0;
                memset(character_data, 0xff, 3200);
        }
        
        if (finished != NULL) {
                layer_mark_dirty(bitmap_layer_get_layer(character_layer));        
                vibes_short_pulse();
                return;
        }
        
        //size_t offset = i * 2;
        
        if (message != NULL) {
                memcpy(&character_data[offset], message->value->data, message->length);
                offset+=BITMAP_WIDTH;
        }
        
        /*Tuplet value = TupletInteger(1, message->value->data[0]);
        DictionaryIterator *iter;
        app_message_out_get(&iter);
        
        if (iter == NULL) {
                return;
        }
        
        dict_write_tuplet(iter, &value);
        dict_write_end(iter);
        
        app_message_out_send();
        app_message_out_release();*/
        //memcpy(character_data + offset, message->value->data + 1, message->length - 1);
   //text_layer_set_text(&hello_layer, message->value->cstring);
}

void in_dropped(AppMessageResult reason, void *context) {
        /*if (reason == APP_MSG_CALLBACK_NOT_REGISTERED) {
                text_layer_set_text(&hello_layer, "Hello not registered!");
        } else if (reason == APP_MSG_CALLBACK_ALREADY_REGISTERED) {
                text_layer_set_text(&hello_layer, "Hello already registered!");
        } else if (reason == APP_MSG_ALREADY_RELEASED) {
                text_layer_set_text(&hello_layer, "Hello already released!");
        }else if (reason == APP_MSG_BUFFER_OVERFLOW) {
                text_layer_set_text(&hello_layer, "Hello buff over!");
        }else if (reason == APP_MSG_BUSY) {
                text_layer_set_text(&hello_layer, "Hello busy!");
        }else if (reason == APP_MSG_INVALID_ARGS) {
                text_layer_set_text(&hello_layer, "Hello invalid args!");
        }else if (reason == APP_MSG_APP_NOT_RUNNING) {
                text_layer_set_text(&hello_layer, "Hello not running!");
        }else if (reason == APP_MSG_OK) {
                text_layer_set_text(&hello_layer, "Hello ok!");
        }else if (reason == APP_MSG_NOT_CONNECTED) {
                text_layer_set_text(&hello_layer, "Hello not connected!");
        }else if (reason == APP_MSG_SEND_REJECTED) {
                text_layer_set_text(&hello_layer, "Hello send rejected!");
        }else if (reason == APP_MSG_SEND_TIMEOUT) {
                text_layer_set_text(&hello_layer, "Hello send timeout!");
        }else {
                text_layer_set_text(&hello_layer, "Hello Dropped!");
        }*/
}

static void handle_init() {
    window = window_create();
    window_stack_push(window, true /* Animated */);

  /*text_layer_init(&hello_layer, GRect(0, 65, 144, 30));
text_layer_set_text_alignment(&hello_layer, GTextAlignmentCenter);
text_layer_set_text(&hello_layer, "Hello World!");
text_layer_set_font(&hello_layer, fonts_get_system_font(FONT_KEY_ROBOTO_CONDENSED_21));
layer_add_child(&window.layer, &hello_layer.layer);

        text_layer_set_text(&hello_layer, "Hello New!");*/
                                                                                         
        
    memset(character_data, 0xff, 3200);
        
        // Album art
    character_bitmap = (GBitmap) {
        .addr = character_data,
        .bounds = GRect(0, 0, 144, 160),
        .info_flags = 1,
        .row_size_bytes = 20,
    };
    //memset(album_art_data, 0, 512);
    character_layer = bitmap_layer_create(GRect(0, 0, 144, 160));
    
    bitmap_layer_set_background_color(character_layer, GColorWhite);
    bitmap_layer_set_bitmap(character_layer, &character_bitmap);
	
	Layer *window_layer = window_get_root_layer(window);
    layer_add_child(window_layer, bitmap_layer_get_layer(character_layer));

	app_message_register_inbox_received(in_received);
	app_message_register_inbox_dropped(in_dropped);
	
	app_message_open(256, 32);
}

static void handle_deinit(void) {
	bitmap_layer_destroy(character_layer);
	window_destroy(window);
}

int main(void) {
	  handle_init();
	  app_event_loop();
	  handle_deinit();
}