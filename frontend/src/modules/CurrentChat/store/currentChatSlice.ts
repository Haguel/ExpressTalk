import { PayloadAction, createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import axios from "axios";
import { chatGetUrls } from "config";
import { CurrentChat } from "../types/CurrentChat";

interface InitialState {
  data: CurrentChat | null;
  status: "idle" | "loading" | "error" | "fulfilled";
  errorMessage: string | null;
}

const initialState: InitialState = {
  data: null,
  status: "idle",
  errorMessage: null,
};

const currentChatSlice = createSlice({
  name: "@@currentChat",
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(getCurrentChat.fulfilled, (state, action: PayloadAction<CurrentChat>) => {
        state.status = "fulfilled";
        state.data = action.payload;
      })
      .addCase(getCurrentChat.pending, (state) => {
        state.status = "loading";
        state.errorMessage = null;
      })
      .addCase(getCurrentChat.rejected, (state, action: PayloadAction<any>) => {
        state.status = "error";
        state.errorMessage =
          action.payload.response?.data?.message || action.payload.message;
      });
  },
});

export const getCurrentChat = createAsyncThunk<any, string>(
  "@@currentChat/getCurrentChat",
  async (id, { rejectWithValue }) => {
    try {
      const { data } = await axios.get(chatGetUrls.privateChat(id), {
        withCredentials: true,
      });

      return data;
    } catch (error) {
      rejectWithValue(error);
    }
  }
);

export const {} = currentChatSlice.actions;

export default currentChatSlice.reducer;
