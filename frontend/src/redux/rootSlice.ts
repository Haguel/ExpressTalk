import { createAsyncThunk, createSlice, PayloadAction } from "@reduxjs/toolkit";
import axios from "axios";
import { userGetUrls, userPostUrls } from "config";
import { CurrentChatType } from "types/CurrentChatType";
import { IUser } from "types/IUser";

interface InitialState {
  currentUser: {
    status: "idle" | "loading" | "error" | "fulfilled";
    user: IUser | null;
    errorCode: number | null;
  };
  avatarUrl: string | null;
  currentChatId: string | null;
  isCreatedNewChat: boolean;
  currentChatType: CurrentChatType;
}

const initialState: InitialState = {
  currentUser: {
    status: "idle",
    user: null,
    errorCode: null,
  },
  avatarUrl: null,
  currentChatId: null,
  isCreatedNewChat: false,
  currentChatType: "privateChat",
};

const rootSlice = createSlice({
  name: "@@root",
  initialState,
  reducers: {
    setCurrentChatId: (state, action: PayloadAction<string>) => {
      state.currentChatId = action.payload;
    },
    setIsCreatedNewChat: (state, action: PayloadAction<boolean>) => {
      state.isCreatedNewChat = action.payload;
    },
    setCurrentChatType: (state, action: PayloadAction<CurrentChatType>) => {
      state.currentChatType = action.payload;
    },
    resetChatId: (state) => {
      state.currentChatId = null;
    },
  },
  extraReducers(builder) {
    builder
      .addCase(getCurrentUser.fulfilled, (state, action: PayloadAction<any>) => {
        state.currentUser.status = "fulfilled";
        state.currentUser.user = action.payload;
      })
      .addCase(getCurrentUser.pending, (state) => {
        state.currentUser.status = "loading";
      })
      .addCase(getCurrentUser.rejected, (state, action: PayloadAction<any>) => {
        state.currentUser.status = "error";
        state.currentUser.errorCode = action.payload.response?.status || null;
      });
  },
});

export const getCurrentUser = createAsyncThunk<any, void>(
  "@@root/getCurrentUser",
  async (_, { rejectWithValue }) => {
    try {
      const { data } = await axios.get(userGetUrls.self, { withCredentials: true });

      return data;
    } catch (error) {
      return rejectWithValue(error);
    }
  }
);

export const getUserAvatar = createAsyncThunk<string, string>(
  "@@/getUserAvatar",
  async (userId, { rejectWithValue }) => {
    try {
      const { data } = await axios.get(userGetUrls.avatar(userId), { withCredentials: true });

      return data;
    } catch (error) {
      return rejectWithValue(error);
    }
  }
);

export const editCurrentUser = createAsyncThunk<any, { name: string; login: string }>(
  "@@/editCurrentUser",
  async (newUserData, { rejectWithValue }) => {
    try {
      const { data } = await axios.post(userPostUrls.edit, newUserData, { withCredentials: true });

      return data;
    } catch (error) {
      return rejectWithValue(error);
    }
  }
);

export const editUserAvatar = createAsyncThunk<string, any>(
  "@@/editAvatar",
  async (formData, { rejectWithValue }) => {
    try {
      const { data } = await axios.post(userPostUrls.avatar, formData, {
        withCredentials: true,
      });

      return data.imageId;
    } catch (error) {
      return rejectWithValue(error);
    }
  }
);

export const { setCurrentChatId, setIsCreatedNewChat, setCurrentChatType, resetChatId } = rootSlice.actions;

export default rootSlice.reducer;
